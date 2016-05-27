package com.vita.sjk.zhihudaily;

import android.app.Application;
import android.content.Context;

/**
 * Created by sjk on 2016/5/27.
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getMyApplicationContext() {
        return context;
    }
}
