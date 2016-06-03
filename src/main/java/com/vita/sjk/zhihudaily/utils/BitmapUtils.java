package com.vita.sjk.zhihudaily.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.io.InputStream;

/**
 * Created by sjk on 2016/5/29.
 */
public class BitmapUtils {

    /**
     * 如果是BitmapDrawable的实例，那么可以直接getBitmap
     * 如果不是，那就得用canvas画到bitmap上再返回
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            //LogUtils.log("drawable is an instance of BitmapDrawable.");
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            //LogUtils.log("drawable is NOT an instance of BitmapDrawable!");
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
     *
     * @param res    资源文件，要根据context获得。传入此参数是为了适配屏幕。
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawable(Resources res, Bitmap bitmap) {
        BitmapDrawable ret = new BitmapDrawable(res, bitmap);
        return ret;
    }


    /**
     * 把bitmap尽量控制在某个width和height的大小范围内
     * 用于生成缩略图，省内存和本地空间
     * 不过只适用于本地资源图片改变大小，不适合网络
     *
     * @param res
     * @param resourceId
     * @param requestWidth
     * @param requestHeight
     * @return
     */
    public static Bitmap getResizedBitmap(Resources res, int resourceId, int requestWidth, int requestHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resourceId, options); // 这一行之后，options参数被赋值
        int w = options.outWidth;
        int h = options.outHeight;
        int inSampleSize = 1;   // 跟缩小的倍数差不多，不过还是不一样，系统会优化成2的倍数
        if (w > requestWidth || h > requestHeight) {
            int ratioWidth = Math.round((float) w / requestWidth);
            int ratioHeight = Math.round((float) h / requestHeight);
            inSampleSize = ratioWidth > ratioHeight ? ratioHeight : ratioWidth; // 取小的
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeResource(res, resourceId, options);
    }
}
