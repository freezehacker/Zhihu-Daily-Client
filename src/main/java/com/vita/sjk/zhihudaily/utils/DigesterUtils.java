package com.vita.sjk.zhihudaily.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by sjk on 2016/5/28.
 */
public class DigesterUtils {

    /**
     * 因为url有很多不规则的字符，所以该函数
     * 用于将url转成0~F的字符串，有唯一性，便于作为key存储
     * 具体算法是，首先是MD5，然后再映射到16进制
     * (参考郭霖老师的博客关于LruCache的帖子)
     *
     * @param originStr
     * @return
     */
    public static String getHash(String originStr) {
        String ret;
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsae) {
            LogUtils.log(nsae.getMessage());
        }
        messageDigest.update(originStr.getBytes());
        byte[] result = messageDigest.digest();
        StringBuilder sb = new StringBuilder();
        for (int i = 0, siz = result.length; i < siz; ++i) {
            String tmp = Integer.toHexString(result[i] & 0xFF);
            if (tmp.length() == 1) {
                sb.append('0');
            }
            sb.append(tmp);
        }
        return sb.toString();
    }
}
