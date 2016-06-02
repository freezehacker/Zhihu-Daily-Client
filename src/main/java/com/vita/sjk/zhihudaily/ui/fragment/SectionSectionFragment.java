package com.vita.sjk.zhihudaily.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.adapter.FirstAdapter;
import com.vita.sjk.zhihudaily.adapter.LatestListAdapter;
import com.vita.sjk.zhihudaily.api.API;
import com.vita.sjk.zhihudaily.bean.ResponseLatest;
import com.vita.sjk.zhihudaily.bean.ResponseSection;
import com.vita.sjk.zhihudaily.bean.Story;
import com.vita.sjk.zhihudaily.constants.Constants;
import com.vita.sjk.zhihudaily.ui.NewsShowActivity;
import com.vita.sjk.zhihudaily.utils.HttpUtils;
import com.vita.sjk.zhihudaily.utils.LogUtils;

import org.apache.http.params.HttpParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sjk on 2016/6/3.
 *
 * ViewPager中的子fragment
 */
public class SectionSectionFragment extends BaseFragment implements LatestListAdapter.OnItemClickListener {

    public static final String SECTION_TYPE = "sectionType";

    private List<Story> storyList = new ArrayList<>();

    private RecyclerView recyclerView;

    private LatestListAdapter adapter;

    /**
     * 构造器
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
     * ViewPager切换的时候，onCreate只会调用一次，所以适合在这里请求网络数据
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNewData();
    }

    /**
     * ViewPager切换的时候，onCreateView会调用多次，不适合网络请求
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section_2, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.section_2_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        adapter = new LatestListAdapter(getActivity(), R.layout.fragment_list_item, storyList, recyclerView);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    /**
     * 网络请求，也相当于刷新
     */
    private void requestNewData() {
        int type = getArguments().getInt(SECTION_TYPE);
        String urlString = String.format(API.GET_NEWS_BY_THEME, String.valueOf(type));
        HttpUtils.httpGetJsonString(urlString, new HttpUtils.HttpCallback() {
            @Override
            public void onFinish(String jsonString) {
                ResponseSection response = new Gson().fromJson(jsonString, ResponseSection.class);
                storyList.clear();
                storyList.addAll(response.getStories());
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(getActivity(), NewsShowActivity.class);

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
}
