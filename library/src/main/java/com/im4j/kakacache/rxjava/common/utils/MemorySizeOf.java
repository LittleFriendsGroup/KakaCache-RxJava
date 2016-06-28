package com.im4j.kakacache.rxjava.common.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 计算内存大小
 * @version alafighting 2016-06
 *
 * FIXME 修复计算方式
 */
public final class MemorySizeOf {

    public interface SizeOf {
        long sizeOf();
    }

    private MemorySizeOf() {
    }

    /**
     * 计算大小
     */
    public static long sizeOf(SizeOf obj) {
        return obj.sizeOf();
    }

    /**
     * 计算大小
     */
    public static long sizeOf(Serializable obj) {
        long size = -1;
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();  //缓冲流
            size = baos.size();
        } catch (FileNotFoundException e) {
            // TODO log
            e.printStackTrace();
        } catch (IOException e) {
            // TODO log
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ignored) {
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }
            }
        }
        return size;
    }


    /**
     * 计算大小
     */
    public static long sizeOf(Bitmap bitmap) {
        long size = -1;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            size = baos.size();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }
            }
        }
        return size;
    }

}
