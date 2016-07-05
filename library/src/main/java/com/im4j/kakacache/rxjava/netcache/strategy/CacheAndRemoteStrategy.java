package com.im4j.kakacache.rxjava.netcache.strategy;

import com.im4j.kakacache.rxjava.netcache.ResultData;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 先缓存，后网络
 * @version alafighting 2016-06
 */
public class CacheAndRemoteStrategy extends BasicCacheStrategy {

    @Override
    public <T> Observable<ResultData<T>> execute(String key, Observable<T> source) {
        Observable<ResultData<T>> cache = loadCache(key);
        Observable<ResultData<T>> remote = loadRemote(key, source);
        return Observable.concat(cache, remote)
                .filter(result -> result.data != null)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io());
    }

}
