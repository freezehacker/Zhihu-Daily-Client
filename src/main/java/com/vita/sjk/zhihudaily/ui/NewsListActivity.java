package com.vita.sjk.zhihudaily.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.adapter.FirstAdapter;
import com.vita.sjk.zhihudaily.api.API;
import com.vita.sjk.zhihudaily.bean.ResponseLatest;
import com.vita.sjk.zhihudaily.bean.Story;
import com.vita.sjk.zhihudaily.constants.Constants;
import com.vita.sjk.zhihudaily.utils.CacheUtils;
import com.vita.sjk.zhihudaily.utils.HttpUtils;
import com.vita.sjk.zhihudaily.utils.LogUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by sjk on 2016/5/27.
 * 新闻列表
 */
public class NewsListActivity extends BaseActivity {


    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;


    /**
     * 标记是否是刚打开app
     */
    private boolean isFirstEnter = true;
    /**
     * 作为adapter参数的关键的列表
     */
    private List<Story> storyList;
    /**
     * RecyclerView的适配器
     */
    private FirstAdapter adapter;
    /**
     * 用来记录哪些新闻（id唯一标记）已经被浏览过
     * 浏览过的新闻，其标题会变成灰色，以提升用户体验
     */
    private static SparseBooleanArray newsHasRead; // Integer-->Boolean


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        initViews();

        httpGetData();
    }

    @Override
    protected void onDestroy() {
        /**
         * 释放资源的操作
         */
        super.onDestroy();
    }

    private void initViews() {
        progressBar = (ProgressBar) findViewById(R.id.my_pb);
        progressBar.setVisibility(View.VISIBLE);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.my_srl);
        swipeRefreshLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                R.color.yellow,
                android.R.color.holo_green_light,
                R.color.cyan,
                android.R.color.holo_blue_light,
                R.color.purple
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /**
                 * 刷新数据，除了有一点点操作和第一次进入Activity时不一样，其实就是和它一样再次请求网络
                 */
                httpGetData();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.my_rv);
        recyclerView.setVisibility(View.GONE);
    }

    /**
     * 加载网络数据，始终获得最新的数据
     * 在最初进入activity和下拉刷新的时候触发
     */
    private void httpGetData() {
        HttpUtils.httpGetJsonString(API.GET_LATEST_NEWS, new HttpUtils.HttpCallback() {
            @Override
            public void onFinish(final String jsonString) {
                /**
                 * 需要运行在UI线程
                 */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * 打印出来debug下
                         */
                        //LogUtils.log("onFinish()");
                        LogUtils.log(jsonString);

                        ResponseLatest bean = new Gson().fromJson(jsonString, ResponseLatest.class);
                        storyList = bean.getStories();
                        LogUtils.log(storyList.size() + "");

                        buildRecyclerView();

                        /**
                         * 如果是第一次进入Activity，则要让进度条消失并且显示RecyclerView
                         */
                        if (isFirstEnter) {
                            showRecyclerView();
                        }

                        /**
                         * 如果是下拉刷新，则要关闭下拉图标
                         */
                        if (!isFirstEnter) {
                            swipeRefreshLayout.setRefreshing(false);
                            Snackbar.make(swipeRefreshLayout, "刷新完成", Snackbar.LENGTH_SHORT).show();
                        }

                        if (isFirstEnter) {
                            isFirstEnter = false;
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                LogUtils.log("onError() is called:\n" + message);
            }
        });
    }

    /**
     * RecyclerView部分的配置
     */
    private void buildRecyclerView() {
        /**
         * 第一次进入的时候构建RecyclerView，后面就不用再次创建，只需要更新adapter
         */
        if (isFirstEnter) {
            recyclerView.setHasFixedSize(true); // 据说是提高性能
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(llm);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }

        adapter = new FirstAdapter(this, R.layout.item_news_list__first, storyList, recyclerView);
        adapter.setOnItemClickListener(new FirstAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(NewsListActivity.this, NewsShowActivity.class);
                /**
                 * 只需要传新闻的id就可以了
                 */
                long id = storyList.get(position).getId();
                LogUtils.log("跳转到新闻的id=" + id);
                intent.putExtra(Constants.NEWS_ID, id);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
        //LogUtils.log("adapter set");
    }

    /**
     * 建立好RecyclerView好，展示给用户看
     * 注意是在UI线程操作控件
     */
    private void showRecyclerView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }
                if (recyclerView.getVisibility() == View.GONE) {
                    recyclerView.setVisibility(View.VISIBLE);
                }
                if (swipeRefreshLayout.getVisibility() == View.GONE) {
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 该函数包含了一系列操作
     * 操作：用假数据填充
     * 目的：看item布局是否安排好了
     */
    private void testItemLayout() {
        // 貌似现在不需要了
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete_cache) {
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
            CacheUtils.clearDiskCache();
        }
        return true;
    }
}
