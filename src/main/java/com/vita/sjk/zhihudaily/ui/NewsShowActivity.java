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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.vita.sjk.zhihudaily.utils.ThreadPoolUtils;

import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Created by sjk on 2016/5/28.
 */
public class NewsShowActivity extends BaseActivity {

    CollapsingToolbarLayout news_collasping_toolbar_layout;
    Toolbar news_toolbar;
    NestedScrollView news_nested_scroll_view;
    TextView news_content_text;
    WebView news_content_web_view;

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

    private boolean isError = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_show);

        Intent intent = getIntent();
        news_id = intent.getLongExtra(Constants.NEWS_ID, Constants.NEWS_ID_INVALID);
        /**
         * 这里的type是指是否有html的body
         * 有，就是0
         * 没有，就是1
         */
        news_type = intent.getIntExtra(Constants.NEWS_TYPE, Constants.NEWS_TYPE_INVALID);
        news_title = intent.getStringExtra(Constants.NEWS_TITLE);

        /**
         * 姑且认为，
         * 如果id出错了，那传输intent的信息就全失效了
         */
        if (news_id == Constants.NEWS_ID_INVALID) {
            LogUtils.log("Intent携带信息出错");
            isError = true;
            /**
             * 可否在这里finish?
             */
            finish();
        }

        /**
         * 没有出错才能继续进行，如果出错呢？能不能在onCreate里直接finish()?
         */
        if (!isError) {
            initViews();
            initVars();
            callHttpToShowNews(news_id);
        }
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
     * 重写返回按键的方法
     * 因为该activity要么是用webView显示，要么是用textView，所以要区分两种情况
     * 用webView时，是在页面栈中操作
     * 而用textView时，是在任务栈中操作
     */
    @Override
    public void onBackPressed() {
        if (news_content_web_view.getVisibility() == View.VISIBLE && news_content_web_view.canGoBack()) {
            news_content_web_view.goBack();
        } else {
            finish();
        }
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        int rand = RandomGenerator.getRandomInt(0, bg_colors.length);
        news_nested_scroll_view = (NestedScrollView) findViewById(R.id.news_nested_scroll_view);
        news_content_text = (TextView) findViewById(R.id.news_content_text);
        news_content_web_view = (WebView) findViewById(R.id.news_content_web_view);
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
                finish();
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

                if (news.getType() == API.TYPE_WITH_HTML_BODY) {
                    /**
                     * 有html_body，需要去解析，然后用textView显示
                     */
                    html_body = news.getBody(); // 获取html的内容，赋给'全局'变量
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            news_content_web_view.setVisibility(View.GONE);
                            news_content_text.setVisibility(View.VISIBLE);
                            /**
                             * 刷新TextView内容，只能这样刷新，别的什么invalidate都是无效的……
                             */
                            news_content_text.setText(Html.fromHtml(html_body, mNewsImageGetter, null));
                        }
                    });
                } else {
                    /**
                     * 没有html_body，那就直接用一个webView显示
                     */
                    final String url = news.getShare_url();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            news_content_text.setVisibility(View.GONE);
                            news_content_web_view.setVisibility(View.VISIBLE);
                            news_content_web_view.setWebViewClient(new WebViewClient() {
                                @Override
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    view.loadUrl(url);
                                    /**
                                     * 返回值：
                                     *  true，直接在本webView浏览
                                     *  false，调用其他浏览器打开浏览
                                     */
                                    return true;
                                }
                            });
                            news_content_web_view.getSettings().setJavaScriptEnabled(true);
                            news_content_web_view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                            news_content_web_view.loadUrl(url);
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                LogUtils.log(message);
            }
        });

    }

    /**
     * 加上同步锁
     *
     * @param task
     */
    private synchronized void addTask(NewsImageTask task) {
        taskSet.add(task);
    }

    /**
     * 加上同步锁
     *
     * @param task
     */
    private synchronized void removeTask(NewsImageTask task) {
        taskSet.remove(task);
    }

    /**
     * 实现接口，下载并缓存html中的图片
     */
    class NewsImageGetter implements Html.ImageGetter {
        @Override
        public Drawable getDrawable(String source) {
            Drawable ret = null;
            //Drawable ret = NewsShowActivity.this.getDrawable(R.drawable.no_pic);
            /**
             * 先从缓存中查看，如果有的话就直接返回，没有的话就先设置empty然后异步下载
             */
            Bitmap bitmap = CacheUtils.load(source);
            if (bitmap != null) {
                //LogUtils.log("图片在缓存中找到：" + source);
                ret = BitmapUtils.bitmapToDrawable(getResources(), bitmap);
                ret.setBounds(0, 0, ret.getIntrinsicWidth(), ret.getIntrinsicHeight());
                return ret;
            }
            //LogUtils.log("图片并没有在缓存中找到!：" + source + ", 线程id:" + Thread.currentThread().getId());
            if (!taskSet.contains(source)) {
                new NewsImageTask(source).executeOnExecutor(ThreadPoolUtils.getInstance(), source);
                //new NewsImageTask(source).execute(source);
            }
            return ret;
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
            addTask(NewsImageTask.this);
        }

        @Override
        protected Drawable doInBackground(String... params) {
            LogUtils.log("即将下载图片url为：" + urlStr);
            Drawable ret = null;
            try {
                /**
                 * 下载
                 */
                Bitmap bitmap = BitmapFactory.decodeStream(new URL(urlStr).openStream());
                /**
                 * 这里要考虑是否耗时，尤其是当图片多起来的时候，每一次都要转化成drawable...
                 */
                ret = BitmapUtils.bitmapToDrawable(getResources(), bitmap);

                /*ret = Drawable.createFromStream(is, urlString);
                ret.setBounds(0, 0, ret.getIntrinsicWidth(), ret.getIntrinsicHeight());*/
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
            //taskSet.remove(NewsImageTask.this);

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
             * 这里有毒……
             */
            news_content_text.setText(Html.fromHtml(html_body, mNewsImageGetter, null));
            LogUtils.log("刷新TextView，图片url为：" + urlStr);
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
