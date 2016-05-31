package com.vita.sjk.zhihudaily.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vita.sjk.zhihudaily.R;
import com.vita.sjk.zhihudaily.ui.BaseActivity;
import com.vita.sjk.zhihudaily.utils.BitmapUtils;
import com.vita.sjk.zhihudaily.utils.LogUtils;

import java.net.URL;

/**
 * Created by sjk on 2016/5/31.
 */
public class TestImageActivity extends BaseActivity {

    final String urlString = "http://pic2.zhimg.com/70/0b731d1f61c0e4a994d5266cc7568a25_b.jpg";

    URL url = null;
    Drawable drawable = null;
    Bitmap bitmap = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Toolbar toolbar = new Toolbar(this);
        setSupportActionBar(toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.log("click on toolbar");
            }
        });
        */


        LinearLayout container = new LinearLayout(this);
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        container.setOrientation(LinearLayout.VERTICAL);

        final ImageView iv_drawable = new ImageView(this);
        iv_drawable.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        iv_drawable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.log("click on ImageView(drawable)");
            }
        });
        final ImageView iv_bitmap = new ImageView(this);
        iv_bitmap.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        iv_bitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.log("click on ImageView(bitmap)");
            }
        });

        /**
         * 网络访问，异步（worker线程）
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    url = new URL(urlString);
                    /*
                    drawable = Drawable.createFromStream(url.openStream(), null);
                    bitmap = BitmapUtils.drawableToBitmap(drawable);
                    */
                    bitmap = BitmapFactory.decodeStream(url.openStream());
                    drawable = BitmapUtils.bitmapToDrawable(getResources(), bitmap);

                    LogUtils.log(String.format("Bitmap size: (%d, %d)", bitmap.getWidth(), bitmap.getHeight()));
                    LogUtils.log(String.format("Drawable size: (%d, %d)", drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));

                    // 更新UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_bitmap.setImageBitmap(bitmap);
                            iv_drawable.setImageDrawable(drawable);
                        }
                    });
                } catch (Exception e) {

                } finally {

                }
            }
        }).start();


        container.addView(iv_bitmap);
        container.addView(iv_drawable);
        setContentView(container);
    }
}
