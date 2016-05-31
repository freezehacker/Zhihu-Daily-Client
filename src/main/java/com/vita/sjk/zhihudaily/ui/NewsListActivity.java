package com.vita.sjk.zhihudaily.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AbsListView;
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
public class NewsListActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener, FirstAdapter.OnItemClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private long exitTime = 0;

    /**
     * 数据源
     * 作为adapter参数的关键的列表
     */
    private List<Story> storyList = null;
    /**
     * RecyclerView的适配器
     */
    private FirstAdapter adapter = null;
    /**
     * 用来记录哪些新闻（id唯一标记）已经被浏览过
     * 浏览过的新闻，其标题会变成灰色，以提升用户体验
     */
    private static SparseBooleanArray newsHasRead = null; // Integer-->Boolean


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        initViews();

        initVars();

        bindViews();

        //httpRefreshData();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        onRefresh();
    }

    @Override
    protected void onDestroy() {
        /**
         * 释放资源的操作
         */
        super.onDestroy();
    }

    /**
     * 初始化控件视图
     */
    private void initViews() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.my_srl);
        recyclerView = (RecyclerView) findViewById(R.id.my_rv);
        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * RecyclerView定位到最顶端
                 */
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    /**
     * 初始化控件的逻辑行为
     */
    private void bindViews() {
        /**
         * swipeRefreshLayout
         */
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                R.color.yellow,
                android.R.color.holo_green_light,
                R.color.cyan,
                android.R.color.holo_blue_light,
                R.color.purple
        );
        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * recyclerView
         */
        recyclerView.setHasFixedSize(true); // 据说是提高性能
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        /**
         * toolBar
         * 设置双击回到顶部
         */
        toolbar.setOnTouchListener(new View.OnTouchListener() {

            long lastTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        long curTime = System.currentTimeMillis();
                        if (curTime - lastTime < Constants.DOUBLE_CLICK_INTERVAL) {
                            /**
                             * 双击会让recyclerView回到顶部，即下面函数所述的“平滑滚动到第0个位置”
                             * 调用别的例如setY setScrollY scrollTo等等，都没用
                             */
                            recyclerView.smoothScrollToPosition(0);
                        } else {
                            lastTime = curTime;
                        }
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 初始化实例变量
     */
    private void initVars() {

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buildAdapter();

                        /**
                         * 获得数据后，在视图上，停止刷新图标
                         */
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                            Snackbar snackbar = Snackbar.make(swipeRefreshLayout, "刷新成功", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(Color.GREEN);
                            snackbar.setActionTextColor(Color.WHITE);
                            snackbar.show();
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
     * Adapter部分的配置
     */
    private void buildAdapter() {
        adapter = new FirstAdapter(this, R.layout.item_news_list__first, storyList, recyclerView);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
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

    /**
     * 下拉刷新的回调
     */
    @Override
    public void onRefresh() {
        httpRefreshData();
    }

    /**
     * item点击的回调
     *
     * @param v
     * @param position 点击的item位置
     */
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

    /**
     * 用户按后退键触发的回调
     */
    @Override
    public void onBackPressed() {
        /**
         * 先检测是不是在刷新
         * 如果是，那就只是关闭刷新
         */
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        long curTime = System.currentTimeMillis();
        if (curTime - exitTime > Constants.EXIT_TIME_INTERVAL) {
            exitTime = curTime;
            Toast.makeText(NewsListActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

}
