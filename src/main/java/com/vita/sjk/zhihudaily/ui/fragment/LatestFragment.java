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
import com.vita.sjk.zhihudaily.bean.ResponseSection;
import com.vita.sjk.zhihudaily.bean.Story;
import com.vita.sjk.zhihudaily.constants.Constants;
import com.vita.sjk.zhihudaily.ui.NewsShowActivity;
import com.vita.sjk.zhihudaily.utils.HttpUtils;
import com.vita.sjk.zhihudaily.utils.LogUtils;
import com.vita.sjk.zhihudaily.view.TemplateRecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sjk on 2016/6/2.
 */
public class LatestFragment extends BaseFragment {

    /**
     * 表示往前多少天
     * 这个用来计算出日期，从而推算出应该加载以前哪一天的新闻
     */
    private int daysBefore = 0;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    /**
     * 数据源
     * 作为adapter参数的关键的列表
     * 也是一开始就new了
     */
    private List<Story> mStoryList = new ArrayList<>();

    private TemplateRecyclerView recycler;


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
        View view = inflater.inflate(R.layout.fragment_collect, container, false);
        recycler = (TemplateRecyclerView) view.findViewById(R.id.collect_recycler);

        recycler.buildAdapterWithNewRef(mStoryList);

        recycler.setOnDataRefreshListener(new TemplateRecyclerView.OnDataRefreshListener() {
            @Override
            public void onDataRefresh() {
                requestNewData();
            }
        });

        recycler.setOnItemClickListener(new TemplateRecyclerView.TemplateAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                jumpToAnotherActivity(position);
            }
        });

        recycler.setOnLoadMoreListener(new TemplateRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                requestMoreData();
            }
        });

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

        requestNewData();
    }

    /**
     * 操作：刷新列表
     */
    private void requestNewData() {
        String urlString = API.GET_LATEST_NEWS;
        HttpUtils.httpGetJsonString(urlString, new HttpUtils.HttpCallback() {
            @Override
            public void onFinish(String jsonString) {
                ResponseLatest response = new Gson().fromJson(jsonString, ResponseLatest.class);
                mStoryList.clear();
                mStoryList.addAll(response.getStories());   // 最好保持mStoryList引用的对象不变

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recycler.refreshAdapter();
                        recycler.mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), "刷新成功：最新", Toast.LENGTH_SHORT).show();
                        /**
                         * 往前天数归为0
                         */
                        daysBefore = 0;
                    }
                });
            }

            @Override
            public void onError(String message) {
                LogUtils.log(message);
            }
        });
    }

    /**
     * 操作：跳转到新闻显示的activity
     *
     * @param position
     */
    private void jumpToAnotherActivity(int position) {
        Story story = mStoryList.get(position);
        Intent intent = new Intent(getActivity(), NewsShowActivity.class);
        intent.putExtra(Constants.NEWS_ID, story.getId());
        intent.putExtra(Constants.NEWS_TYPE, story.getType());
        intent.putExtra(Constants.NEWS_TITLE, story.getTitle());
        startActivity(intent);
    }

    /**
     * 操作：添加更多新闻
     */
    private void requestMoreData() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -daysBefore);
        ++daysBefore;
        String dateStr = simpleDateFormat.format(calendar.getTime());
        LogUtils.log("请求日期str为: " + dateStr);

        String urlString = String.format(API.GET_NEWS_AT_DATE, dateStr);
        HttpUtils.httpGetJsonString(urlString, new HttpUtils.HttpCallback() {
            @Override
            public void onFinish(String jsonString) {
                ResponseSection response = new Gson().fromJson(jsonString, ResponseSection.class);
                List<Story> moreList = response.getStories();
                final int from = mStoryList.size();
                final int count = moreList.size();
                mStoryList.addAll(moreList);   // 最好保持mStoryList引用的对象不变

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recycler.refreshAdapter(from, count);
                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }
}
