package com.vita.sjk.zhihudaily.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

/**
 * Created by sjk on 2016/5/29.
 */
public class BitmapUtils {

    /**
     * 如果是BitmapDrawable的实例，那么可以直接getBitmap
     * 如果不是，那就得用canvas画到bitmap上再返回
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            LogUtils.log("drawable is an instance of BitmapDrawable.");
            return ((BitmapDrawable)drawable).getBitmap();
        } else {
            LogUtils.log("drawable is NOT an instance of BitmapDrawable!");
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicWidth(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);  // 把drawable画到canvas即bitmap上
            return bitmap;
        }
    }


    /**
     * bitmap转成drawable
     * @param res   资源文件，要根据context获得。传入此参数是为了适配屏幕。
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawable(Resources res, Bitmap bitmap) {
        BitmapDrawable ret = new BitmapDrawable(res, bitmap);
        return ret;
    }
}
