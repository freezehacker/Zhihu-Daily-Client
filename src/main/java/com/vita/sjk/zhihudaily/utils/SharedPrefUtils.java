package com.vita.sjk.zhihudaily.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.vita.sjk.zhihudaily.MyApplication;

/**
 * Created by sjk on 2016/5/28.
 * 试图封装SharedPreferences操作，但是觉得封装起来有点麻烦
 * 暂时搁置……
 */
public class SharedPrefUtils {

    private static final String SP_NAME = "my_shared_pref";
    private static final int SP_MODE = Context.MODE_PRIVATE;

    public static void open(String sharedPrefName) {
        if (TextUtils.isEmpty(sharedPrefName)) {
            throw new IllegalArgumentException("必须给shared pref提供一个名字!");
        }
    }
}
