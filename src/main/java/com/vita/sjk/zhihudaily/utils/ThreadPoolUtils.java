package com.vita.sjk.zhihudaily.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sjk on 2016/5/27.
 * 管理线程池，单例类
 */
public class ThreadPoolUtils {

    private ThreadPoolUtils() {}

    /**
     * 线程池的类型，可自由改变
     */
    private static ExecutorService mInstance = Executors.newFixedThreadPool(3);

    public static ExecutorService getInstance() {
        return mInstance;
    }
}
