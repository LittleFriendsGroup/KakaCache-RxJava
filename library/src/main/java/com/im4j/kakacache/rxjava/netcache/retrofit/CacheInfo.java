package com.im4j.kakacache.rxjava.netcache.retrofit;

import com.im4j.kakacache.rxjava.CACHE;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.netcache.strategy.CacheStrategy;

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

    public static CacheInfo get(Annotation[] annotations) {
        CacheInfo info = new CacheInfo();

        for (Annotation annotation : annotations) {
            if (annotation instanceof CACHE) {
                CACHE cache = (CACHE) annotation;
                info.key = cache.value();
                info.enable = cache.enable();
                info.strategy = cache.strategy();
                break;
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
            LogUtils.log(e);
            return null;
        }

        str.append('-');
        str.append(ByteString.of(request.headers().toString().getBytes()).sha1().hex());

        return str.toString();
    }



    private String key;
    private boolean enable;
    private Class<? extends CacheStrategy> strategy;

    public CacheInfo() {
    }

    @Override
    public String toString() {
        return "CacheInfo{" +
                "key='" + key + '\'' +
                ", enable=" + enable +
                ", strategy=" + strategy.getSimpleName() +
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

    public Class<? extends CacheStrategy> getStrategy() {
        return strategy;
    }

    public void setStrategy(Class<? extends CacheStrategy> strategy) {
        this.strategy = strategy;
    }
}
