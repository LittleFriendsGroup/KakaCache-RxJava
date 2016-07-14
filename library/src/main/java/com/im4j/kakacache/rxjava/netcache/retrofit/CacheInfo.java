package com.im4j.kakacache.rxjava.netcache.retrofit;

import com.im4j.kakacache.rxjava.CACHE;
import com.im4j.kakacache.rxjava.netcache.strategy.CacheStrategy;

import java.lang.annotation.Annotation;

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


    private String key;
    private boolean enable;
    private CacheStrategy strategy;

    public CacheInfo() {
    }

    @Override
    public String toString() {
        return "CacheInfo{" +
                "key='" + key + '\'' +
                ", enable=" + enable +
                ", strategy=" + strategy.name() +
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

    public CacheStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(CacheStrategy strategy) {
        this.strategy = strategy;
    }
}
