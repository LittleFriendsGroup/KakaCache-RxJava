package com.im4j.kakacache.rxjava.common.utils;

import android.graphics.Bitmap;

import com.im4j.kakacache.rxjava.common.exception.Exception;
import com.im4j.kakacache.rxjava.common.exception.NotImplementException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.NotSerializableException;
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
        if (obj == null) {
            return 0;
        }

        return obj.sizeOf();
    }

    /**
     * 计算大小
     */
    public static long sizeOf(Serializable serial) throws Exception {
        if (serial == null) {
            return 0;
        }

        long size = -1;
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(serial);
            oos.flush();  //缓冲流
            size = baos.size();
        } catch (FileNotFoundException e) {
            throw new NotImplementException(e.getMessage());
        } catch (NotSerializableException e) {
            throw new NotImplementException(e.getMessage() + " does not implement the MemorySizeOf.SizeOf.");
        } catch (IOException e) {
            LogUtils.log(e);
        } finally {
            Utils.close(oos);
            Utils.close(baos);
        }
        return size;
    }

    /**
     * 计算大小
     */
    public static long sizeOf(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }

        long size = -1;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            size = baos.size();
        } finally {
            Utils.close(baos);
        }
        return size;
    }

}
