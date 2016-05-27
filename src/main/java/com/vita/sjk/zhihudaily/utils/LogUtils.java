package com.vita.sjk.zhihudaily.utils;

import android.util.Log;

/**
 * Created by sjk on 2016/5/27.
 */
public class LogUtils {

    private LogUtils() {}
    
    public static final String TAG = "what:";
    
    public static void log(String str) {
        if (null == str) {
            str = "[null]";
        }
        Log.d(TAG, str);
        System.out.println(TAG + str);
    }
}
