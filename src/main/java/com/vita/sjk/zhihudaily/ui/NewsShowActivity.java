package com.vita.sjk.zhihudaily.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.webkit.WebView;
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

import org.w3c.dom.Text;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sjk on 2016/5/28.
 */
public class NewsShowActivity extends BaseActivity {

    TextView news_content_text;
    String html_body;

    private NewsImageGetter mNewsImageGetter = null;
    private NewsTagHandler mNewsTagHandler = null;
    private Map<String, NewsImageTask> taskMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_show);

        initViews();

        initVars();

        Intent intent = getIntent();
        int id = intent.getIntExtra(Constants.NEWS_ID, Constants.NEWS_ID_INVALID);
        if (id == Constants.NEWS_ID_INVALID) {
            // 传过来的新闻id不正常，处理异常
        } else {
            callHttpToShowNews(id);
        }
    }

    @Override
    protected void onDestroy() {
        /**
         * 停止一切下载
         */
        for (String key: taskMap.keySet()) {
            taskMap.get(key).cancel(false);  // true or false?
        }

        super.onDestroy();
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        news_content_text = (TextView) findViewById(R.id.news_content_text);
        news_content_text.setMovementMethod(LinkMovementMethod.getInstance());
        news_content_text.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * 初始化‘全局’变量
     */
    private void initVars() {
        mNewsImageGetter = new NewsImageGetter();
        taskMap = new HashMap<>();
    }

    /**
     * 发起网络请求，请求一条新闻的内容
     *
     * @param news_id
     */
    private void callHttpToShowNews(int news_id) {
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
                        showNews();
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
     * 把新闻的标题、内容等显示在UI控件上。也可以看作是更新TextView的操作（因为图片是慢慢加载的，每加载一张就更新一次）。
     * 该操作内不新开worker线程，所以调用的时候记得要运行在UI线程
     */
    private void showNews() {
        /*
        final Spanned spanned = Html.fromHtml(news.getBody(), new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                InputStream is = null;
                try {
                    is = new URL(source).openStream();
                } catch (MalformedURLException mue) {
                    LogUtils.log(mue.getMessage());
                } catch (IOException ioe) {
                    LogUtils.log(ioe.getMessage());
                }
                Drawable ret = Drawable.createFromStream(is, source);   // 第2个参数是？

                // 必须给drawable设边界才能显示在TextView中

                ret.setBounds(0, 0, ret.getIntrinsicWidth() * 4, ret.getIntrinsicHeight() * 4);
                LogUtils.log("" + ret.getIntrinsicWidth() + ", " + ret.getIntrinsicHeight());
                return ret;
            }
        }, null);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                news_content_text.setText(spanned);
            }
        });
        */
        news_content_text.setText(Html.fromHtml(html_body, mNewsImageGetter, null));
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
                Drawable drawable = BitmapUtils.bitmapToDrawable(bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * 4, drawable.getIntrinsicHeight() * 4);
                return drawable;
            } else {
                bitmap = CacheUtils.loadFromDisk(source);
                if (bitmap != null) {
                    //LogUtils.log("disk");
                    Drawable drawable = BitmapUtils.bitmapToDrawable(bitmap);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * 4, drawable.getIntrinsicHeight() * 4);

                    CacheUtils.dumpToMemory(source, bitmap);    // dump到内存中，更快

                    return drawable;
                } else {
                    /**
                     * 异步中的网络下载，下载之后会缓存
                     */
                    //LogUtils.log("download");
                    new NewsImageTask(source).execute(source);
                    /**
                     * 先在主线程返回null，也就是空图片
                     * 这里可以改成是设置成一张准备好的empty图片
                     */
                    return null;
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
         * @param source    原生url，没有经过hash
         */
        public NewsImageTask(String source) {
            urlStr = source;
            /**
             * 加入map，注意这里的urlStr就是原本的url，没有经过hash
             * 所以以后可能会改一下这里
             */
            taskMap.put(urlStr, NewsImageTask.this);
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
                ret = Drawable.createFromStream(is, urlString);
                ret.setBounds(0, 0, ret.getIntrinsicWidth(), ret.getIntrinsicHeight());
            } catch (Exception e) { // 暂时（偷懒）统一处理Exception
                LogUtils.log(e.getMessage());
            } finally {

            }
            return ret;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            /**
             * 删掉任务
             */
            taskMap.remove(urlStr);

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
            showNews();
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
