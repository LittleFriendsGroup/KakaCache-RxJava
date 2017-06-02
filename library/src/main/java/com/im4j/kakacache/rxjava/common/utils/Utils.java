package com.im4j.kakacache.rxjava.common.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.im4j.kakacache.rxjava.common.exception.ArgumentException;
import com.im4j.kakacache.rxjava.common.exception.NullException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import okhttp3.Request;
import okio.Buffer;
import okio.ByteString;

/**
 * 工具类
 */
public final class Utils {

    private Utils() {
    }

    /**
     * 不为空
     */
    public static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullException("Can not be empty.");
        }
        return obj;
    }

    /**
     * 不小于0
     */
    public static long checkNotLessThanZero(long number) {
        if (number < 0) {
            throw new ArgumentException("Can not be < 0.");
        }

        return number;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }


    public static Class<?> getRawType(Type type) {
        if (type == null) throw new NullPointerException("type == null");

        if (type instanceof Class<?>) {
            // Type is a normal class.
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
            // suspects some pathological case related to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>) rawType;
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable) {
            // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
            // type that's more general than necessary is okay.
            return Object.class;
        }
        if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        }

        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    /**
     * 根据Request生成哈希值
     */
    public static String getHash(Request request) {
        StringBuilder str = new StringBuilder();
        str.append('[');
        str.append(request.method());
        str.append(']');
        str.append('[');
        str.append(request.url().toString());
        str.append(']');

        try {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            str.append(buffer.readByteString().sha1().hex());
        } catch (IOException e) {
            L.log(e);
            return "";
        }

        str.append('-');
        str.append(ByteString.of(request.headers().toString().getBytes()).sha1().hex());

        return str.toString();
    }




    /**
     * 获取外部缓存目录
     */
    public static File getExternalCacheDir(Context context) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    /**
     * 获取APP缓存目录
     */
    public static File getCacheDir(Context context) {
        File appCacheDir = context.getCacheDir();
        if(appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    /**
     * 获取可用的缓存目录
     * @tips 优先外置存储
     */
    public static File getUsableCacheDir(Context context) {
        if (hasExternalStorage(context)) {
            return getExternalCacheDir(context);
        } else {
            return getCacheDir(context);
        }
    }

    /**
     * 获取可用的缓存目录
     * @param child 子目录
     * @tips 优先外置存储
     */
    public static File getUsableCacheDir(Context context, String child) {
        File dir = getUsableCacheDir(context);
        if (dir == null) {
            return null;
        }
        return new File(dir, child);
    }


    /**
     * 是否有可用的外置存储
     */
    public static boolean hasExternalStorage(Context context) {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    private static boolean hasPermission(Context context, @NonNull String permission) {
        int perm = context.checkCallingOrSelfPermission(permission);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    public static void close(Closeable close) {
        if (close != null) {
            try {
                closeThrowException(close);
            } catch (IOException ignored) {
            }
        }
    }

    public static void closeThrowException(Closeable close) throws IOException {
        if (close != null) {
            close.close();
        }
    }

}
