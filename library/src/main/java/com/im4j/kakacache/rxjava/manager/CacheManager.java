package com.im4j.kakacache.rxjava.manager;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.core.CacheCore;
import com.im4j.kakacache.rxjava.core.CacheTarget;

/**
 * 缓存管理
 * @version alafighting 2016-04
 * // TODO 超时？
 */
public abstract class CacheManager {

    private CacheCore cache;
    private final Object lock = new Object();

    CacheManager(CacheCore cache) {
        this.cache = cache;
    }


    /**
     * 读取
     */
    <T> T _load(String key) throws CacheException {
        return cache.load(key);
    }

    /**
     * 是否包含
     * @param key
     * @return
     * // TODO throw Exception ?
     */
    boolean _containsKey(String key) {
        return cache.containsKey(key);
    }


    /**
     * 保存
     * @param expires 有效期（单位：秒）
     */
    <T> void _save(String key, T value, int expires, CacheTarget target) throws CacheException {
        // 同步
        synchronized(lock) {
            cache.save(key, value, expires, target);
        }
    }

    /**
     * 删除缓存
     * @param key
     * // TODO return Boolean?
     */
    void _remove(String key) throws CacheException {
        // 同步
        synchronized(lock) {
            cache.remove(key);
        }
    }

    /**
     * 清空缓存
     */
    void _clear() throws CacheException {
        // 同步
        synchronized(lock) {
            cache.clear();
        }
    }



    /**
     * 构造器
     */
    public static abstract class Builder {
        CacheCore cache;

        public Builder(CacheCore cache) {
            this.cache = cache;
        }

        public RxJavaCacheManager.Builder rxjava() {
            return new RxJavaCacheManager.Builder(cache);
        }

    }

}
