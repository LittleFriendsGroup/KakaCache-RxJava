package com.im4j.kakacache.rxjava.manager;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.core.CacheCore;
import com.im4j.kakacache.rxjava.core.CacheTarget;

import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * RxJava模式缓存管理
 * @version alafighting 2016-04
 */
public class RxCacheManager extends CacheManager {

    private static abstract class SimpleSubscribe<T> implements rx.Observable.OnSubscribe<T> {
        @Override
        public final void call(Subscriber<? super T> subscriber) {
            try {
                execute(subscriber);
            } catch (Throwable e) {
                subscriber.onError(e);
                return;
            }

            subscriber.onCompleted();
        }
        abstract void execute(Subscriber<? super T> subscriber);
    }


    public RxCacheManager(CacheCore cache) {
        super(cache);
    }


    /**
     * 读取
     */
    public <T> rx.Observable<T> load(final String key) {
        return rx.Observable.create(new SimpleSubscribe<T>() {
            @Override
            void execute(Subscriber<? super T> subscriber) {
                T value = _load(key);
//                if (value == null) {
//                    subscriber.onError(new NotFoundException("找不到缓存’"+key+"'"));
//                }
                subscriber.onNext(value);
            }
        }).subscribeOn(Schedulers.computation());
    }

    /**
     * 保存
     * @param expires 有效期（单位：秒）
     */
    public <T> rx.Observable<Boolean> save(final String key, final T value, final int expires, final CacheTarget target) {
        return rx.Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            void execute(Subscriber<? super Boolean> subscriber) {
                _save(key, value, expires, target);
                subscriber.onNext(true);
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
            void execute(Subscriber<? super Boolean> subscriber) {
                boolean value = _containsKey(key);
                subscriber.onNext(value);
            }
        });
    }

    /**
     * 删除缓存
     * @param key
     */
    public rx.Observable<Boolean> remove(final String key) {
        return rx.Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            void execute(Subscriber<? super Boolean> subscriber) {
                _remove(key);
                subscriber.onNext(true);
            }
        });
    }

    /**
     * 清空缓存
     */
    public rx.Observable<Boolean> clear() throws CacheException {
        return rx.Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            void execute(Subscriber<? super Boolean> subscriber) {
                _clear();
                subscriber.onNext(true);
            }
        });
    }

}
