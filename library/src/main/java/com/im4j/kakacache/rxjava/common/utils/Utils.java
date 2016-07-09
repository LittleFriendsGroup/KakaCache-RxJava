package com.im4j.kakacache.rxjava.common.utils;

import android.content.Context;
import android.os.Environment;

import com.im4j.kakacache.rxjava.common.exception.ArgumentException;
import com.im4j.kakacache.rxjava.common.exception.InstanceException;
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
 * @version alafighting 2016-04
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


    public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException("length=" + arrayLength
                    + "; regionStart=" + offset
                    + "; regionLength=" + count);
        }
    }

    /**
     * 取父类泛型
     * @param clazz
     * @return 没有则返回null
     */
    @SuppressWarnings("rawtypes")
    public static Type[] getGenericSuperclass(Class<?> clazz) {
        try {
            Type typeGeneric = clazz.getGenericSuperclass();
            if (typeGeneric != null) {
                if (typeGeneric instanceof ParameterizedType) {
                    return getGeneric((ParameterizedType) typeGeneric);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    /**
     * 取父接口泛型
     * @param clazz
     * @return 没有则返回null
     */
    @SuppressWarnings("rawtypes")
    public static Type[] getGenericInterfaces(Class<?> clazz) {
        try {
            Type typeGeneric = clazz.getGenericInterfaces()[0];
            if (typeGeneric != null) {
                if (typeGeneric instanceof ParameterizedType) {
                    return getGeneric((ParameterizedType) typeGeneric);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    /**
     * 取泛型
     * @param type
     * @return 没有则返回null
     */
    @SuppressWarnings("rawtypes")
    public static Type[] getGeneric(ParameterizedType type) {
        try {
            if (type != null) {
                return type.getActualTypeArguments();
            }
        } catch (Exception e) {
        }
        return null;
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

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new InstanceException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new InstanceException(e.getMessage());
        }
    }

    /**
     * 根据Request生成唯一KEY
     * @param request
     * @return
     */
    public static String buildKey(Request request) {
        StringBuilder str = new StringBuilder();
        str.append('[');
        str.append(request.method());
        str.append(']');
        str.append('[');
        str.append(request.url());
        str.append(']');


        try {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            str.append(buffer.readByteString().sha1().hex());
        } catch (IOException e) {
            LogUtils.log(e);
            return "";
        }

        str.append('-');
        str.append(ByteString.of(request.headers().toString().getBytes()).sha1().hex());

        return str.toString();
    }



    public static final String SEPARATOR = File.separator;
    /**
     * 获取缓存目录路径
     * <p><b>
     * ！注意：若不存在，则返回null
     * </b></p>
     * @param context
     * @return 返回:/storage/sdcard0/Android/data/you_packageName/cache/
     */
    public static File getStorageCacheDir(Context context) {
        if (!canUseSDCard()) {
            return getDataCacheDir(context);
        }

        File path = Environment.getExternalStorageDirectory();
        if (path == null) {
            return getDataCacheDir(context);
        }

        return new File(path.getAbsolutePath() + SEPARATOR + "Android" + SEPARATOR + "data" + SEPARATOR
                + context.getPackageName() + SEPARATOR + "cache" + SEPARATOR);
    }
    static File getDataCacheDir(Context context) {
        return context.getCacheDir();
    }

    /**
     * SDCard是否可用
     * <p>PS：一定存在</p>
     * @return
     */
    public static boolean canUseSDCard() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // 是否可写
            return Environment.getExternalStorageDirectory().canWrite();
        }
        return false;
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
