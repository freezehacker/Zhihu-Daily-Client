package com.vita.sjk.zhihudaily.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.vita.sjk.zhihudaily.MyApplication;

/**
 * Created by sjk on 2016/5/28.
 * 试图封装SharedPreferences操作，但是觉得封装起来有点麻烦
 */
public class SharedPrefUtils {

    private static final String SP_NAME = "my_shared_pref";
    private static final int SP_MODE = Context.MODE_PRIVATE;

    public static void dump() {
        SharedPreferences.Editor editor = MyApplication.getMyApplicationContext()
                .getSharedPreferences(SP_NAME, SP_MODE).edit();
    }
}
