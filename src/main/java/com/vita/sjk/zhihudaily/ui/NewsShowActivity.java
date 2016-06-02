package com.vita.sjk.zhihudaily.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.api.API;
import com.vita.sjk.zhihudaily.bean.News;
import com.vita.sjk.zhihudaily.constants.Constants;
import com.vita.sjk.zhihudaily.utils.BitmapUtils;
import com.vita.sjk.zhihudaily.utils.CacheUtils;
import com.vita.sjk.zhihudaily.utils.HttpUtils;
import com.vita.sjk.zhihudaily.utils.LogUtils;
import com.vita.sjk.zhihudaily.utils.RandomGenerator;

import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sjk on 2016/5/28.
 */
public class NewsShowActivity extends BaseActivity {

    CollapsingToolbarLayout news_collasping_toolbar_layout;
    Toolbar news_toolbar;
    NestedScrollView news_nested_scroll_view;
    TextView news_content_text;

    private String html_body;
    private NewsImageGetter mNewsImageGetter = null;
    private NewsTagHandler mNewsTagHandler = null;

    private String news_title = null;
    private int news_type;
    private long news_id;

    private int[] bg_colors = {
            R.color.material_red,
            R.color.material_orange,
            R.color.material_green,
            R.color.material_cyan,
            R.color.material_blue,
            R.color.material_purple,
            R.color.material_light_blue,
            R.color.material_blue_gray,
            R.color.material_gray,
            R.color.material_brown
    };

    /**
     * 用Set管理下载任务
     * 防止有任务重复加进来
     */
    private Set<NewsImageTask> taskSet;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_show);

        Intent intent = getIntent();
        news_id = intent.getLongExtra(Constants.NEWS_ID, Constants.NEWS_ID_INVALID);
        news_type = intent.getIntExtra(Constants.NEWS_TYPE, Constants.NEWS_TYPE_INVALID);
        news_title = intent.getStringExtra(Constants.NEWS_TITLE);

        /**
         * 姑且认为，
         * 如果id出错了，那传输intent的信息就全失效了
         */
        if (news_id == Constants.NEWS_ID_INVALID) {
            LogUtils.log("Intent携带信息出错");
            finish();
        }

        initViews();

        initVars();

        callHttpToShowNews(news_id);
    }

    @Override
    protected void onDestroy() {
        /**
         * 停止一切下载
         */
        for (NewsImageTask task : taskSet) {
            task.cancel(false);  // true or false?
        }

        super.onDestroy();
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        int rand = RandomGenerator.getRandomInt(0, bg_colors.length);
        news_nested_scroll_view = (NestedScrollView) findViewById(R.id.news_nested_scroll_view);
        news_content_text = (TextView) findViewById(R.id.news_content_text);
        ImageView news_block_bg = (ImageView) findViewById(R.id.news_block_bg);
        news_collasping_toolbar_layout = (CollapsingToolbarLayout) findViewById(R.id.news_collasping_toolbar_layout);
        news_toolbar = (Toolbar) findViewById(R.id.news_toolbar);

        /**
         * 随机设置一种颜色，感觉会好一点
         */
        news_block_bg.setImageResource(bg_colors[rand]);

        /**
         * 要显示标题的话，必须设置在collapsingToolbarLayout上，而不是toolBar上
         */
        news_collasping_toolbar_layout.setTitle(news_title);
        news_collasping_toolbar_layout.setContentScrimResource(bg_colors[rand]);

        /**
         * 添加后退导航按钮（原生，不用自己找drawable定义）
         */
        setSupportActionBar(news_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        news_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 导航可以后退（一个activity）
                 */
                onBackPressed();
            }
        });
        news_toolbar.setOnTouchListener(new View.OnTouchListener() {
            long lastTime = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    long curTime = System.currentTimeMillis();
                    if (curTime - lastTime < Constants.DOUBLE_CLICK_INTERVAL) {
                        /**
                         * 双击回到新闻的顶部
                         */
                        news_nested_scroll_view.smoothScrollTo(0, 0);
                    } else {
                        lastTime = curTime;
                    }
                }
                return false;
            }
        });

        news_content_text.setMovementMethod(LinkMovementMethod.getInstance());
        //news_content_text.setMovementMethod(ScrollingMovementMethod.getInstance());

        /**
         * 记得设置scrollvaie的滚动记录，提升用户体验
         */
    }

    /**
     * 初始化‘全局’变量
     */
    private void initVars() {
        mNewsImageGetter = new NewsImageGetter();
        taskSet = new HashSet<>();
    }

    /**
     * 发起网络请求，请求一条新闻的内容
     *
     * @param news_id 新闻id
     */
    private void callHttpToShowNews(long news_id) {
        String urlString = String.format(API.GET_NEWS_BY_ID, String.valueOf(news_id));

        HttpUtils.httpGetJsonString(urlString, new HttpUtils.HttpCallback() {
            @Override
            public void onFinish(String jsonString) {
                Gson gson = new Gson();
                News news = gson.fromJson(jsonString, News.class);

                html_body = news.getBody(); // 获取html的内容，赋给'全局'变量

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        news_content_text.setText(Html.fromHtml(html_body, mNewsImageGetter, null));
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
     * 实现接口，下载并缓存html中的图片
     */
    class NewsImageGetter implements Html.ImageGetter {
        @Override
        public Drawable getDrawable(String source) {
            /**
             * 先从缓存中查看，如果有的话就直接返回，没有的话就先设置empty然后异步下载
             */
            Bitmap bitmap = CacheUtils.loadFromMemory(source);
            if (bitmap != null) {
                //LogUtils.log("memory");
                Drawable drawable = BitmapUtils.bitmapToDrawable(getResources(), bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                return drawable;
            } else {
                bitmap = CacheUtils.loadFromDisk(source);
                if (bitmap != null) {
                    //LogUtils.log("disk");
                    Drawable drawable = BitmapUtils.bitmapToDrawable(getResources(), bitmap);
                    /**
                     * 这里设置drawable大小还是有疑问……
                     * 究竟怎么适配屏幕
                     * 还有图片的位置
                     */
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                    CacheUtils.dumpToMemory(source, bitmap);    // dump到内存中，更快

                    return drawable;
                } else {
                    /**
                     * 异步中的网络下载，下载之后会缓存
                     */
                    //LogUtils.log("download url: " + source);
                    new NewsImageTask(source).execute(source);
                    /**
                     * 先在主线程返回null，也就是空图片
                     * 这里可以改成是设置成一张准备好的empty图片
                     */
                    Drawable ret = NewsShowActivity.this.getDrawable(R.drawable.no_pic);
                    return ret;
                }
            }
        }
    }

    /**
     * 下载图片的worker线程的task
     */
    class NewsImageTask extends AsyncTask<String, Integer, Drawable> {

        String urlStr;

        /**
         * 构造器
         * 把任务放进map中
         *
         * @param source 原生url，没有经过hash
         */
        public NewsImageTask(String source) {
            urlStr = source;
            /**
             * 把任务加入集合加以管理
             */
            taskSet.add(NewsImageTask.this);
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String urlString = params[0];
            Drawable ret = null;
            try {
                /**
                 * 下载
                 */
                URL url = new URL(urlString);
                InputStream is = url.openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                ret = BitmapUtils.bitmapToDrawable(getResources(), bitmap);
                /*
                ret = Drawable.createFromStream(is, urlString);
                ret.setBounds(0, 0, ret.getIntrinsicWidth(), ret.getIntrinsicHeight());
                */
            } catch (Exception e) {
                LogUtils.log(e.getMessage());
            } finally {

            }
            return ret;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            /**
             * 完成后，从清单中删掉本任务
             */
            taskSet.remove(NewsImageTask.this);

            /**
             * 缓存
             */
            if (drawable != null) {
                Bitmap bitmapToDump = BitmapUtils.drawableToBitmap(drawable);
                CacheUtils.dumpToMemory(urlStr, bitmapToDump);
                CacheUtils.dumpToDisk(urlStr, bitmapToDump);
            }

            /**
             * 关键：再给TextView设置一次显示，相当于更新TextView内容
             */
            news_content_text.setText(Html.fromHtml(html_body, mNewsImageGetter, null));
        }
    }

    /**
     * 第三个参数
     */
    class NewsTagHandler implements Html.TagHandler {
        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

        }
    }
}
