package com.vita.sjk.zhihudaily;

import android.app.Application;
import android.content.Context;

import com.vita.sjk.zhihudaily.utils.CacheUtils;

/**
 * Created by sjk on 2016/5/27.
 */
public class MyApplication extends Application {

    private static Context context;

    /**
     * app启动的时候会调用一次
     */
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

    /**
     * 返回全局context，在app任何地方都可以访问
     * @return
     */
    public static Context getMyApplicationContext() {
        return context;
    }
}
