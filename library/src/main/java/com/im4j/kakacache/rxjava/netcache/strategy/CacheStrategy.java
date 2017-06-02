package com.im4j.kakacache.rxjava.netcache.strategy;

import com.im4j.kakacache.rxjava.KakaCache;
import com.im4j.kakacache.rxjava.common.exception.NotFoundException;
import com.im4j.kakacache.rxjava.common.utils.L;
import com.im4j.kakacache.rxjava.netcache.ResultData;
import com.im4j.kakacache.rxjava.netcache.ResultFrom;

import io.reactivex.Observable;
import io.reactivex.internal.operators.single.SingleToObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * 缓存策略
 * @version alafighting 2016-07
 * @version imkarl 2017-02 调整为rx2的语法，优化实现逻辑
 */
public enum CacheStrategy {
    /** 仅缓存 */
    OnlyCache{
        @Override
        <T> Observable<ResultData<T>> execute(String key,
                                              Observable<ResultData<T>> cache,
                                              Observable<ResultData<T>> remote) {
            return cache;
        }
    },
    /** 仅网络 */
    OnlyRemote{
        @Override
        <T> Observable<ResultData<T>> execute(String key,
                                              Observable<ResultData<T>> cache,
                                              Observable<ResultData<T>> remote) {
            return remote;
        }
    },

    /** 优先缓存 */
    FirstCache{
        @Override
        <T> Observable<ResultData<T>> execute(String key,
                                              Observable<ResultData<T>> cache,
                                              Observable<ResultData<T>> remote) {
            cache = cache.onErrorReturnItem(new ResultData<>(ResultFrom.Cache, key, null));
            return new SingleToObservable<>(Observable.concat(cache, remote)
                    .filter(it -> it != null && it.data != null)
                    .first(new ResultData<>(null, key, null)));
        }
    },
    /** 优先服务器 */
    FirstRemote{
        @Override
        <T> Observable<ResultData<T>> execute(String key,
                                              Observable<ResultData<T>> cache,
                                              Observable<ResultData<T>> remote) {
            remote = remote.onErrorReturnItem(new ResultData<>(ResultFrom.Remote, key, null));
            return new SingleToObservable<>(Observable.concat(remote, cache)
                    .filter(it -> it != null && it.data != null)
                    .first(new ResultData<>(null, key, null)));
        }
    },
    /** 先缓存，后网络 */
    CacheAndRemote{
        @Override
        <T> Observable<ResultData<T>> execute(String key,
                                              Observable<ResultData<T>> cache,
                                              Observable<ResultData<T>> remote) {
            cache = cache.onErrorReturnItem(new ResultData<>(ResultFrom.Cache, key, null));
            return Observable.concat(cache, remote)
                    .filter(result -> result != null && result.data != null);
        }
    };



    public final <T> Observable<ResultData<T>> execute(String key, Observable<T> source, int expires) {
        Observable<ResultData<T>> cache = KakaCache.manager().load(key)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(it -> {
                    L.debug("loadCache result="+it);
                    return new ResultData<>(ResultFrom.Cache, key, (T) it);
                });
        Observable<ResultData<T>> remote = source
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(it -> {
                    L.debug("loadRemote result="+it);
                    KakaCache.manager()
                            .save(key, it, expires)
                            .subscribe(status -> L.debug("save status => "+status), L::debug);
                    return new ResultData<>(ResultFrom.Remote, key, it);
                });

        return execute(key, cache, remote)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(it -> {
                    if (it == null || it.data == null) {
                        return Observable.error(new NotFoundException("load data is null."));
                    } else {
                        return Observable.just(it);
                    }
                });
    }

    abstract <T> Observable<ResultData<T>> execute(String key,
                                                   Observable<ResultData<T>> cache,
                                                   Observable<ResultData<T>> remote);

}
