package com.vita.sjk.zhihudaily.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.api.API;
import com.vita.sjk.zhihudaily.bean.ResponseLatest;
import com.vita.sjk.zhihudaily.bean.ResponseSection;
import com.vita.sjk.zhihudaily.bean.Story;
import com.vita.sjk.zhihudaily.constants.Constants;
import com.vita.sjk.zhihudaily.ui.NewsShowActivity;
import com.vita.sjk.zhihudaily.utils.HttpUtils;
import com.vita.sjk.zhihudaily.utils.LogUtils;
import com.vita.sjk.zhihudaily.view.TemplateRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sjk on 2016/6/2.
 */
public class CollectFragment extends BaseFragment {

    TemplateRecyclerView recycler;

    /**
     * 数据源
     * 注意，外部数据源跟adapter的数据源最好是同一个对象！否则很难管理，也容易出错
     * 所以也不要改变引用的对象了
     */
    List<Story> mStoryList = new ArrayList<>();

    public static CollectFragment newInstance() {
        CollectFragment ret = new CollectFragment();
        return ret;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collect, container, false);
        recycler = (TemplateRecyclerView) view.findViewById(R.id.collect_recycler);

        /**
         * 关键！把数据源等同起来
         */
        recycler.buildAdapterWithNewRef(mStoryList);

        recycler.setOnDataRefreshListener(new TemplateRecyclerView.OnDataRefreshListener() {
            @Override
            public void onDataRefresh() {
                LogUtils.log("下拉刷新了");
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * 一进来就请求网络
         */
        LogUtils.log("准备发起请求");
        requestNewData();
    }

    private void requestNewData() {
        String urlString = API.GET_LATEST_NEWS;
        HttpUtils.httpGetJsonString(urlString, new HttpUtils.HttpCallback() {
            @Override
            public void onFinish(String jsonString) {
                LogUtils.log("OK!内容有:" + jsonString.length() + "-->" + jsonString.substring(0, 100));

                ResponseLatest response = new Gson().fromJson(jsonString, ResponseLatest.class);
                mStoryList.clear();
                mStoryList.addAll(response.getStories());   // 最好保持mStoryList引用的对象不变
                LogUtils.log("current list size=" + mStoryList.size());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recycler.refreshAdapter();
                        recycler.mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), "OK", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                LogUtils.log(message);
            }
        });
    }

    private void jumpToAnotherActivity(int position) {
        LogUtils.log("点击了第" + position + "个，准备跳转");

        Story story = mStoryList.get(position);
        Intent intent = new Intent(getActivity(), NewsShowActivity.class);
        intent.putExtra(Constants.NEWS_ID, story.getId());
        intent.putExtra(Constants.NEWS_TYPE, story.getType());
        intent.putExtra(Constants.NEWS_TITLE, story.getTitle());
        startActivity(intent);
    }

    private void requestMoreData() {
        String urlString = String.format(API.GET_NEWS_BY_THEME, 10);
        HttpUtils.httpGetJsonString(urlString, new HttpUtils.HttpCallback() {
            @Override
            public void onFinish(String jsonString) {
                LogUtils.log("OK!\n" + jsonString.substring(0, 100));

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
