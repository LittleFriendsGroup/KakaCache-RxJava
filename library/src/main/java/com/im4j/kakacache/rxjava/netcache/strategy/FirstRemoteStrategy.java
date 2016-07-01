package com.im4j.kakacache.rxjava.netcache.strategy;

import com.im4j.kakacache.rxjava.netcache.ResultData;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 优先服务器
 * @version alafighting 2016-06
 */
public class FirstRemoteStrategy<T> extends BasicCacheStrategy<T> {

    @Override
    public Observable<ResultData<T>> execute(String key, Observable source) {
        return Observable.concat(
                loadRemote(key, source),
                loadCache(key)
        ).firstOrDefault(null, it -> ((ResultData<T>)it).data != null).subscribeOn(Schedulers.io());
    }

}
