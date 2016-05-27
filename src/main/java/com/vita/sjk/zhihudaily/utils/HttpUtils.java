package com.vita.sjk.zhihudaily.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by sjk on 2016/5/27.
 */
public class HttpUtils {


    private static final int READ_TIMEOUT = 5000;

    private static final int CONNECT_TIMEOUT = 3000;


    /**
     * 该接口用于异步回调（返回字符串）
     *
     * 注意，这里是设计成在worker线程进行回调的
     * 所以如果要操作或者更新UI等就需要在调用者（Activity）操作在UI线程上
     * 否则会出现莫名其妙的bug。
     *
     * 这里不设计成直接返回UI线程，是因为难以获取调用者的Context
     * 如果强行获取调用者Context，有可能导致内存泄漏
     */
    public interface HttpCallback {
        void onFinish(String jsonString);

        void onError(String message);

        //void onProgress(int progress);
    }

    /**
     * 同上。
     * 该接口用于异步回调（返回位图）
     * maybe可以继续优化，毕竟一张bitmap的内存开销巨大
     */
    public interface HttpBitmapCallback {
        void onBitmapGot(Bitmap bitmap);

        void onBitmapProgress(int progress);

        void onBitmapError(String message);
    }


    /**
     * 发起get请求，返回json字符串
     *
     * @param urlString
     * @param cb
     */
    public static void httpGetJsonString(final String urlString, final HttpCallback cb) {
        if (cb == null) {
            throw new IllegalArgumentException("2nd argument 'HttpCallback' shouldn't be null!");
        }

        ThreadPoolUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(urlString);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(CONNECT_TIMEOUT);
                    conn.setReadTimeout(READ_TIMEOUT);
                    int responseCode = conn.getResponseCode();

                    /**
                     * 这里最好判断一下状态码是否为200
                     */
                    if (responseCode != 200) {
                        cb.onError("Error: response code != 200:\n" + conn.getResponseMessage());
                    } else {
                        InputStream is = conn.getInputStream();

                        /**
                         * 这里需要顾及InputStreamReader的编码问题吗?
                         */
                        reader = new BufferedReader(new InputStreamReader(is));

                        /**
                         * StringBuilder or StringBuffer?
                         * StringBuffer是线程安全的，而这里用了线程池，so.
                         */
                        StringBuffer sb = new StringBuffer();
                        String chunk;
                        while ((chunk = reader.readLine()) != null) {
                            sb.append(chunk);
                        }
                        cb.onFinish(sb.toString());
                    }
                } catch (MalformedURLException mue) {
                    LogUtils.log(mue.getMessage());
                } catch (IOException ioe) {
                    LogUtils.log(ioe.getMessage());
                } finally {
                    /**
                     * 关闭连接
                     */
                    if (conn != null) {
                        conn.disconnect();
                    }

                    /**
                     * 关闭BufferReader资源(竟然还要处理异常)
                     */
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ioe) {
                            LogUtils.log(ioe.getMessage());
                        }
                    }
                }
            }
        });
    }

    /**
     * 发起get请求，返回图片流
     *
     * @param urlString
     * @param cb
     */
    public static void httpGetBitmapStream(final String urlString, final HttpBitmapCallback cb) {
        if (cb == null) {
            throw new IllegalArgumentException("2nd argument 'HttpBitmapCallback' shouldn't be null!");
        }

        /**
         * 启用线程池下载图片
         */
        ThreadPoolUtils.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(urlString);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(CONNECT_TIMEOUT);
                    conn.setReadTimeout(READ_TIMEOUT);
                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        cb.onBitmapError("Error: response code != 200:\n" + conn.getResponseMessage());
                    } else {
                        InputStream is = conn.getInputStream();
                        int wholeLength = conn.getContentLength(), hasLength = 0, length;
                        byte[] buffer_of_1KB = new byte[1024];

                        /**
                         * 最好给ByteArrayOutputStream定个容量，更省内存
                         * 但是不知道它的容量跟网络流的size的联系是什么，所以不好确定
                         */
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        while ((length = is.read(buffer_of_1KB)) != -1) {
                            hasLength += length;
                            int progress = Math.round((float) hasLength / wholeLength);

                            /**
                             * 通知进度
                             */
                            cb.onBitmapProgress(progress);
                            baos.write(buffer_of_1KB, 0, length);
                        }
                        byte[] bytes = baos.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        cb.onBitmapGot(bitmap);
                    }
                } catch (Exception e) {
                    LogUtils.log(e.getMessage());
                } finally {
                    /**
                     * 断开连接
                     */
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
    }
}
