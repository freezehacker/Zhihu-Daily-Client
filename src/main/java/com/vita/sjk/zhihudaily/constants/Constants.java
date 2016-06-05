package com.vita.sjk.zhihudaily.constants;

import android.content.Context;

/**
 * Created by sjk on 2016/5/28.
 */
public class Constants {

    //----------------------------------------------------------------------------------
    /**
     * 以下的这些常量
     * 是启动NewsShowActivity时用Intent传过去的key
     * 以及如果传送输错的默认value
     */
    public static final String NEWS_ID = "news_id";
    public static final long NEWS_ID_INVALID = -1;

    public static final String NEWS_TITLE = "news_title";
    public static final String NEWS_TITLE_INVALID = "@{null}";

    public static final String NEWS_TYPE = "news_type";
    public static final int NEWS_TYPE_INVALID = -1;

    //----------------------------------------------------------------------------------

    /**
     * 按两次退出之间的有效间隙
     */
    public static final long EXIT_TIME_INTERVAL = 1200;

    /**
     * 双击的间隙定义，两次按下屏幕之间的间隙
     */
    public static final long DOUBLE_CLICK_INTERVAL = 500;

    //----------------------------------------------------------------------------------
    /**
     * 偏好设置(包括订阅的专栏，等)放在SharedPreference内，下面是名称
     */
    public static final String SHARED_PREF_SETTINGS = "shared_pref_settings";
    public static final int SHARED_PREF_SETTINGS_MODE = Context.MODE_PRIVATE;
}
