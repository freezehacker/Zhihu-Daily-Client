package com.vita.sjk.zhihudaily.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;
import com.vita.sjk.zhihudaily.MyApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by sjk on 2016/5/28.
 */
public class CacheUtils {

    public static final long MAX_MEMORY = Runtime.getRuntime().maxMemory();

    public static final String cacheDirString = "bitmap";

    public static String diskCachePathString;

    /**
     * 内存缓存
     * 疑问：如果存的是Drawable会不会比Bitmap更省内存？
     */
    private static LruCache<String, Bitmap> mLruCache;

    /**
     * 硬盘缓存
     */
    private static DiskLruCache mDiskLruCache;

    /**
     * 初始化两个缓存
     * 只在在Application的onCreate中调用，这样可以保证只进行一次
     */
    public static void initCache() {
        /**
         * 初始化内存缓存
         */
        //LogUtils.log("MAX_MEMORY=" + MAX_MEMORY);
        int memory = (int)(MAX_MEMORY / 8);
        mLruCache = new LruCache<String, Bitmap>(memory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };

        /**
         * 初始化硬盘缓存
         */
        Context context = MyApplication.getMyApplicationContext();
        LogUtils.log("全局context=" + context);
        String dirString;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {
            dirString = context.getExternalCacheDir().getPath();
        } else {
            dirString = context.getCacheDir().getPath();
        }
        diskCachePathString = dirString + File.separator + cacheDirString;
        File dir = new File(diskCachePathString);

        int appVersion;
        try {
            appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appVersion = 1;
        }

        try {
            mDiskLruCache = DiskLruCache.open(dir, appVersion, 1, (int)(MAX_MEMORY / 8));
        } catch (IOException ioe) {
            LogUtils.log(ioe.getMessage());
        }
    }

    /**
     * 隐藏构造函数。其实这个构造函数也不需要
     */
    private CacheUtils() {}



    /**
     * 写入内存lruCache中
     * @param key   注意这个key是原生的url，还没有经过hash的，所以要在以下几个函数里都要经过hash
     * @param bitmap
     */
    public static void dumpToMemory(String key, Bitmap bitmap) {
        String hashKey = DigesterUtils.getHash(key);
        if (mLruCache.get(hashKey) == null) {
            mLruCache.put(hashKey, bitmap);
        }
    }

    /**
     * 从内存缓存中载入
     * @param key
     */
    public static Bitmap loadFromMemory(String key) {
        String hashKey = DigesterUtils.getHash(key);
        return mLruCache.get(hashKey);
    }

    /**
     * 写入diskLruCache中
     * 这里用异步，因为文件操作也可能是耗时的
     * @param key
     * @param bitmap
     */
    public static void dumpToDisk(String key, final Bitmap bitmap) {
        final String hashKey = DigesterUtils.getHash(key);
        ThreadPoolUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                DiskLruCache.Editor editor = null;
                try {
                    editor = mDiskLruCache.edit(hashKey);
                    OutputStream os = editor.newOutputStream(0);
                    boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    if (success) {
                        /**
                         * 可以这么理解：
                         * 如果压缩到输出流成功，
                         * 那就从输出流再传输到文件中
                         */
                        editor.commit();
                    } else {
                        editor.abort();
                    }
                } catch (IOException ioe) {
                    LogUtils.log(ioe.getMessage());
                }
            }
        });
    }

    /**
     * 从磁盘中读取
     * 想要写成worker线程，但是怎么返回呢？
     * 暂时写成在UI线程。待修改
     * @param key
     * @return
     */
    public static Bitmap loadFromDisk(String key) {
        Bitmap ret = null;
        String k = DigesterUtils.getHash(key);
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskLruCache.get(k);
            if (snapshot != null) {
                InputStream is = snapshot.getInputStream(0);
                ret = BitmapFactory.decodeStream(is);
            }
        } catch (IOException ioe) {
            LogUtils.log(ioe.getMessage());
        }
        return ret;
    }

}
