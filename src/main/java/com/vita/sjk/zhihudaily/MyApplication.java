package com.vita.sjk.zhihudaily;

import android.app.Application;
import android.content.Context;

import com.vita.sjk.zhihudaily.utils.CacheUtils;

/**
 * Created by sjk on 2016/5/27.
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        /**
         * 初始化缓存，只在这里初始化一次
         * 会用到context对象，就使用上面那个全局context
         */
        CacheUtils.initCache();
    }

    public static Context getMyApplicationContext() {
        return context;
    }
}
