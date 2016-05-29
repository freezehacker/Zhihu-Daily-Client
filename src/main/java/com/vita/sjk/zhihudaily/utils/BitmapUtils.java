package com.vita.sjk.zhihudaily.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicWidth(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);  // 把drawable画到canvas即bitmap上
        return bitmap;
    }

    /**
     * 虽然已经弃用了，但暂时找不到更好的办法
     * @param bitmap
     * @return
     */
    @SuppressWarnings("Deprecated")
    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }
}
