package com.vita.sjk.zhihudaily.constants;

/**
 * Created by sjk on 2016/5/28.
 */
public class Constants {

    /**
     * 传过去的intent中新闻id的key
     * 以及，失效的id的默认值，用来标记出现错误
     */
    public static final String NEWS_ID = "id";
    public static final long NEWS_ID_INVALID = -1;

    /**
     * 按两次退出之间的有效间隙
     */
    public static final long EXIT_TIME_INTERVAL = 1200;

    /**
     * 双击的间隙定义，两次按下屏幕之间的间隙
     */
    public static final long DOUBLE_CLICK_INTERVAL = 500;
}
