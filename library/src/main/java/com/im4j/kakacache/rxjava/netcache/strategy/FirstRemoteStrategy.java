package com.im4j.kakacache.rxjava.netcache.strategy;

import com.im4j.kakacache.rxjava.netcache.ResultData;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 优先服务器
 * @version alafighting 2016-06
 */
public class FirstRemoteStrategy extends BasicCacheStrategy {

    @Override
    public <T> Observable<ResultData<T>> execute(String key, Observable<T> source) {
        Observable<ResultData<T>> cache = loadCache(key);
        Observable<ResultData<T>> remote = loadRemote(key, source);
        return Observable.concat(remote, cache)
                .firstOrDefault(null, it -> it.data != null)
                .subscribeOn(Schedulers.io());
    }

}
