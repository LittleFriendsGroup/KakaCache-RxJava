package com.im4j.kakacache.rxjava.manager;

import com.im4j.kakacache.rxjava.common.exception.NotFoundException;
import com.im4j.kakacache.rxjava.common.utils.L;
import com.im4j.kakacache.rxjava.core.CacheCore;
import com.im4j.kakacache.rxjava.core.CacheTarget;

import io.reactivex.Observable;

/**
 * RxJava模式缓存管理
 * @version alafighting 2016-04
 */
public class RxCacheManager {

    private CacheCore cache;
    private int defaultExpires;

    /**
     * 构造函数
     * @param defaultExpires 默认有效期（毫秒）
     */
    public RxCacheManager(CacheCore cache, int defaultExpires) {
        this.cache = cache;
        this.defaultExpires = defaultExpires;
    }

    /**
     * 读取
     */
    public <T> Observable<T> load(final String key) {
        return Observable.just(key).map(it -> {
            L.debug("loadCache  key="+it);
            try {
                T result = cache.load(it);
                if (result != null) {
                    return result;
                }
            } catch (Throwable e) {
                L.debug(e);
            }
            throw new NotFoundException("load cache is null.");
        });
    }

    /**
     * 保存
     */
    public <T> Observable<Boolean> save(String key, T value) {
        return save(key, value, defaultExpires, CacheTarget.MemoryAndDisk);
    }
    /**
     * 保存
     * @param expires 有效期（单位：毫秒）
     */
    public <T> Observable<Boolean> save(final String key, final T value, final int expires) {
        return save(key, value, expires, CacheTarget.MemoryAndDisk);
    }
    /**
     * 保存
     * @param expires 有效期（单位：毫秒）
     */
    public <T> Observable<Boolean> save(String key, final T value, final int expires, final CacheTarget target) {
        return Observable.just(key).map(it -> {
            try {
                cache.save(it, value, expires, target);
                return true;
            } catch (Exception e) {
                L.debug(e);
                return false;
            }
        });
    }

    /**
     * 是否包含
     */
    public Observable<Boolean> containsKey(final String key) {
        return Observable.just(key).map(it -> {
            try {
                return cache.containsKey(it);
            } catch (Exception e) {
                L.debug(e);
                return false;
            }
        });
    }

    /**
     * 删除缓存
     */
    public Observable<Boolean> remove(final String key) {
        return Observable.just(key).map(it -> {
            try {
                cache.remove(key);
                return true;
            } catch (Exception e) {
                L.debug(e);
                return false;
            }
        });
    }

    /**
     * 清空缓存
     */
    public Observable<Boolean> clear() {
        return Observable.just(0).map(it -> {
            try {
                cache.clear();
                return true;
            } catch (Exception e) {
                L.debug(e);
                return false;
            }
        });
    }

}
