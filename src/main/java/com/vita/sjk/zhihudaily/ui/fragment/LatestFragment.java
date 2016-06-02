package com.vita.sjk.zhihudaily.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.adapter.LatestListAdapter;
import com.vita.sjk.zhihudaily.api.API;
import com.vita.sjk.zhihudaily.bean.ResponseLatest;
import com.vita.sjk.zhihudaily.bean.Story;
import com.vita.sjk.zhihudaily.constants.Constants;
import com.vita.sjk.zhihudaily.ui.NewsShowActivity;
import com.vita.sjk.zhihudaily.utils.HttpUtils;
import com.vita.sjk.zhihudaily.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sjk on 2016/6/2.
 */
public class LatestFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener,
        LatestListAdapter.OnItemClickListener,
        LatestListAdapter.OnLoadMoreListener {

    /**
     * 表示往前多少天
     * 这个用来计算出日期，从而推算出应该加载以前哪一天的新闻
     */
    private int daysBefore = 0;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    /**
     * 滑动组件
     */
    private int lastVisibleItemPos = 0;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * 数据源
     * 作为adapter参数的关键的列表
     */
    private List<Story> storyList = null;

    /**
     * RecyclerView的适配器
     */
    private LatestListAdapter adapter = null;

    /**
     * 用来记录哪些新闻（id唯一标记）已经被浏览过
     * 浏览过的新闻，其标题会变成灰色，以提升用户体验
     */
    private SparseBooleanArray newsHasRead = null; // Integer-->Boolean


    /**
     * 构造该Fragment的一个实例
     *
     * @return
     */
    public static LatestFragment newInstance() {
        LatestFragment ret = new LatestFragment();
        return ret;
    }

    /**
     * 初始化view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_latest, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_latest);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_latest);
        return view;
    }

    /**
     * 在这里定义view的行为，分担onCreateView的压力
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * swipeRefreshLayout部分的配置
         */
        swipeRefreshLayout.setColorSchemeResources(
                R.color.material_red,
                R.color.material_orange,
                R.color.material_yellow,
                R.color.material_green,
                R.color.material_cyan,
                R.color.material_blue,
                R.color.material_purple,
                R.color.material_light_blue,
                R.color.material_blue_gray,
                R.color.material_gray,
                R.color.material_brown
        );
        swipeRefreshLayout.setOnRefreshListener(this);

        //实现刚开始进入的时候的自动刷新
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        onRefresh();

        /**
         * recyclerView部分的配置
         */
        //recyclerView.setHasFixedSize(true); // 据说是提高性能
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // 这里实现"上拉加载更多"（未实现）
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                /**
                 * 当滑动到最后一个item的时候，自动加载更多
                 */
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPos + 1 == adapter.getItemCount()) {
                    httpLoadMoreData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                lastVisibleItemPos = linearLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    private void buildAdapterAfterGettingData() {
        adapter = new LatestListAdapter(getActivity(), R.layout.fragment_list_item, storyList, recyclerView);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }


    /**
     * 加载网络数据，始终获得最新的数据
     * 在最初进入activity和下拉刷新的时候触发
     */
    private void httpRefreshData() {
        HttpUtils.httpGetJsonString(API.GET_LATEST_NEWS, new HttpUtils.HttpCallback() {
            @Override
            public void onFinish(final String jsonString) {
                LogUtils.log(jsonString);
                ResponseLatest bean = new Gson().fromJson(jsonString, ResponseLatest.class);
                storyList = bean.getStories();
                /**
                 * 需要运行在UI线程
                 */
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * 重新设置adapter
                         * 疑问：不知道notifyDatasetChanged在RecyclerView中会不会失效？
                         * 如果不会的话以后就用那个，不需要像下面那样每次都new一个
                         */
                        buildAdapterAfterGettingData();

                        /**
                         * 获得数据后，在视图上，停止刷新图标
                         */
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getActivity(), "刷新成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                LogUtils.log("onError():\n" + message);
            }
        });
    }

    /**
     * 根据从今天开始不断往后的日期，请求更多的（旧）新闻数据
     */
    private void httpLoadMoreData() {
        /**
         * 根据日期推算出字符串
         */
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -daysBefore);
        ++daysBefore;
        String dateStr = simpleDateFormat.format(calendar.getTime());
        LogUtils.log("加载日期为:" + dateStr);   // 打印一下

        /**
         * 发起网络请求
         */
        String urlString = String.format(API.GET_NEWS_AT_DATE, dateStr);
        HttpUtils.httpGetJsonString(urlString, new HttpUtils.HttpCallback() {
            @Override
            public void onFinish(String jsonString) {
                LogUtils.log("请求成功!");
                ResponseLatest response = new Gson().fromJson(jsonString, ResponseLatest.class);
                List<Story> extraStories = response.getStories();
                final int positionStart = storyList.size();
                final int extraSize = extraStories.size();
                storyList.addAll(extraStories);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemRangeInserted(positionStart, extraSize);
                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    /**
     * 下拉刷新的回调
     */
    @Override
    public void onRefresh() {
        httpRefreshData();
    }

    /**
     * ItemClick回调
     * 即点击新闻，跳转到新闻显示的activity
     *
     * @param v
     * @param position 点击的item位置
     */
    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(getActivity(), NewsShowActivity.class);
        /**
         * 在根据id搜到正文之前，可以先传递标题、类型，让NewsShowActivity事先显示，像知乎那样
         */
        Story story = storyList.get(position);
        long id = story.getId();
        String title = story.getTitle();
        int type = story.getType();

        LogUtils.log("跳转到新闻的id=" + id);
        intent.putExtra(Constants.NEWS_ID, id);
        intent.putExtra(Constants.NEWS_TITLE, title);
        intent.putExtra(Constants.NEWS_TYPE, type);

        startActivity(intent);
    }

    /**
     * “上拉加载更多”的回调
     */
    @Override
    public void onLoadMore() {

    }
}
