package com.im4j.kakacache.rxjava.netcache.retrofit;

import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.netcache.CACHE;

import java.io.IOException;
import java.lang.annotation.Annotation;
import okhttp3.Request;
import okio.Buffer;
import okio.ByteString;

/**
 * 缓存信息
 * @version alafighting 2016-07
 */
public class CacheInfo {

    private String key;
    private boolean enable;

    public CacheInfo() {
    }


    public static CacheInfo get(Annotation[] annotations) {
        CacheInfo info = new CacheInfo();
        info.key = null;
        info.enable = false;

        for (Annotation annotation : annotations) {
            if (annotation instanceof CACHE) {
                CACHE cache = (CACHE) annotation;
                info.key = cache.value();
                info.enable = cache.enable();
            }
        }

        return info;
    }
    public static String buildKey(Request request) {
        StringBuffer str = new StringBuffer();
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
            LogUtils.e(e);
            return null;
        }

        str.append('-');
        str.append(ByteString.of(request.headers().toString().getBytes()).sha1().hex());

        return str.toString();
    }



    @Override
    public String toString() {
        return "CacheInfo{" +
                "key='" + key + '\'' +
                ", enable=" + enable +
                '}';
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
