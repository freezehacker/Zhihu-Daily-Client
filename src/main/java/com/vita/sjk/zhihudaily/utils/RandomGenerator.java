package com.vita.sjk.zhihudaily.utils;

/**
 * Created by sjk on 2016/6/1.
 */
public class RandomGenerator {

    /**
     *
     * @param validLeft 有效左边界，可以取到
     * @param invalidRight  无效右边界，取不到。即取整数属于[validLeft, invalidRight)
     * @return
     */
    public static int getRandomInt(int validLeft, int invalidRight) {
        return (int)(Math.random() * (invalidRight - validLeft) + validLeft);
    }
}
