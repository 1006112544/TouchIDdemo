package com.daobao.asus.touchiddemo.base64util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 转换编码
 */

public class Base64Object {
    /**
     * 把base64的String码转换成正常显示的字符串
     */
    public static String base64ToString(String base64) {
        byte[] decode = Base64Util.decode(base64);
        String s = new String(decode);
        return s;
    }
    /**
     * 把String的转换成base64码
     */
    public static String stringToBase64(String ss) {
        byte[] bytes = ss.getBytes();
        String encode = Base64Util.encode(bytes);
        return encode;
    }
    /**
     * bitmap转为base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase641(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}