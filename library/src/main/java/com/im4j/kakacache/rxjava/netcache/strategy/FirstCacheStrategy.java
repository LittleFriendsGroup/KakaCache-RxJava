package com.im4j.kakacache.rxjava.netcache.strategy;

import com.im4j.kakacache.rxjava.netcache.ResultData;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 优先缓存
 * @version alafighting 2016-06
 */
public class FirstCacheStrategy<T> extends BasicCacheStrategy<T> {

    @Override
    public Observable<ResultData<T>> execute(String key, Observable source) {
        return Observable.concat(
                loadCache(key),
                loadRemote(key, source)
        ).first(it -> ((ResultData<T>)it).data != null).subscribeOn(Schedulers.io());
    }

}
