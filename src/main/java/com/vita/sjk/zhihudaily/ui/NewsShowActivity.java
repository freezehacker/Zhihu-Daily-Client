package com.vita.sjk.zhihudaily.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import com.vita.sjk.zhihudaily.utils.HttpUtils;
import com.vita.sjk.zhihudaily.utils.LogUtils;

import org.w3c.dom.Text;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by sjk on 2016/5/28.
 */
public class NewsShowActivity extends BaseActivity {

    TextView news_content_text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_show);

        initViews();

        Intent intent = getIntent();
        int id = intent.getIntExtra(Constants.NEWS_ID, Constants.NEWS_ID_INVALID);
        if (id == Constants.NEWS_ID_INVALID) {
            // 处理异常
        } else {
            callHttpToShowNews(id);
        }
    }

    private void initViews() {
        news_content_text = (TextView) findViewById(R.id.news_content_text);
        news_content_text.setMovementMethod(LinkMovementMethod.getInstance());
        news_content_text.setMovementMethod(ScrollingMovementMethod.getInstance());
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

                showNews(news);
            }

            @Override
            public void onError(String message) {
                LogUtils.log(message);
            }
        });

    }

    /**
     * 把新闻的标题、内容等显示在UI控件上
     * 其中，内容中的图片还是要下载一遍的，不过这里ImageGetter本来就处于worker线程中了
     * 所以，只需要在最后显示在TextView中的操作运行在UI线程上就可以了
     */
    private void showNews(final News news) {
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
    }
}
