package com.vita.sjk.zhihudaily.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sjk on 2016/6/3.
 * <p/>
 * ViewPager中的子fragment
 */
public class SectionSectionFragment extends BaseFragment {

    public static final String SECTION_TYPE = "sectionType";

    private TemplateRecyclerView recycler;

    private List<Story> mStoryList = new ArrayList<>();

    private boolean isFirstEnter = true;

    /**
     * 构造器
     *
     * @param sectionType
     * @return
     */
    public static SectionSectionFragment newInstance(int sectionType) {
        SectionSectionFragment ret = new SectionSectionFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(SECTION_TYPE, sectionType);
        ret.setArguments(bundle);
        return ret;
    }

    /**
     * ViewPager切换的时候，onCreateView会调用多次，不适合网络请求
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section_2, container, false);
        recycler = (TemplateRecyclerView) view.findViewById(R.id.section_section_recycler);
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
        /*
        // 由于API有限，上拉加载暂时不支持
        recycler.setOnLoadMoreListener(new TemplateRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                requestMoreData();
            }
        });
        */
        return view;
    }

    /**
     * SectionSectionFragment的实例是在ViewPager里面的
     * 所以每一次滑动都有可能调用一次
     * 故要加一个判断，判断是否第一次进入，以减少网络请求
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //LogUtils.log("onActivityCreated from Fragment(" + this.hashCode() + ")");
        /**
         * 第一次进入才自动刷新
         */
        if (isFirstEnter) {
            isFirstEnter = false;
            requestNewData();
        }
    }

    /**
     * 操作：刷新列表
     */
    private void requestNewData() {
        final int type = getArguments().getInt(SECTION_TYPE);
        String urlString = String.format(API.GET_NEWS_BY_THEME, String.valueOf(type));
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
                        Toast.makeText(getActivity(), "刷新成功：" + API.SECTION[type], Toast.LENGTH_SHORT).show();
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
     * 由于API有限，暂时不支持
     */
    private void requestMoreData() {
        final int type = getArguments().getInt(SECTION_TYPE);
        String urlString = String.format(API.GET_NEWS_BY_THEME, String.valueOf(type));
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
