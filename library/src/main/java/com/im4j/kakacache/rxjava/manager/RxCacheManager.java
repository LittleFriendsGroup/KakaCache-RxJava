package com.im4j.kakacache.rxjava.manager;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.core.CacheCore;
import com.im4j.kakacache.rxjava.core.CacheTarget;

import rx.Subscriber;
import rx.exceptions.Exceptions;

/**
 * RxJava模式缓存管理
 * @version alafighting 2016-04
 */
public class RxCacheManager {

    private static abstract class SimpleSubscribe<T> implements rx.Observable.OnSubscribe<T> {
        @Override
        public final void call(Subscriber<? super T> subscriber) {
            try {
                T data = execute();
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(data);
                }
            } catch (Throwable e) {
                LogUtils.log(e);
                Exceptions.throwIfFatal(e);
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
                return;
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }
        abstract T execute() throws Throwable;
    }


    private final Object lock = new Object();

    private CacheCore cache;
    private int defaultExpires;

    /**
     * 构造函数
     * @param cache
     * @param defaultExpires 默认有效期（毫秒）
     */
    public RxCacheManager(CacheCore cache, int defaultExpires) {
        this.cache = cache;
        this.defaultExpires = defaultExpires;
    }

    /**
     * 读取
     */
    public <T> rx.Observable<T> load(final String key) {
        return rx.Observable.create(new SimpleSubscribe<T>() {
            @Override
            T execute() {
                LogUtils.debug("loadCache  key="+key);
                return cache.load(key);
            }
        });
    }

    /**
     * 保存
     */
    public <T> rx.Observable<Boolean> save(String key, T value) {
        return save(key, value, defaultExpires, CacheTarget.MemoryAndDisk);
    }
    /**
     * 保存
     * @param expires 有效期（单位：秒）
     */
    public <T> rx.Observable<Boolean> save(final String key, final T value, final int expires, final CacheTarget target) {
        return rx.Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                // 同步
                synchronized(lock) {
                    cache.save(key, value, expires, target);
                }
                return true;
            }
        });
    }

    /**
     * 是否包含
     * @param key
     * @return
     */
    public rx.Observable<Boolean> containsKey(final String key) {
        return rx.Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                return cache.containsKey(key);
            }
        });
    }

    /**
     * 删除缓存
     * @param key
     * // TODO return Boolean?
     */
    public rx.Observable<Boolean> remove(final String key) {
        return rx.Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                synchronized(lock) {
                    cache.remove(key);
                }
                return true;
            }
        });
    }

    /**
     * 清空缓存
     */
    public rx.Observable<Boolean> clear() throws CacheException {
        return rx.Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                // 同步
                synchronized(lock) {
                    cache.clear();
                }
                return true;
            }
        });
    }

}
