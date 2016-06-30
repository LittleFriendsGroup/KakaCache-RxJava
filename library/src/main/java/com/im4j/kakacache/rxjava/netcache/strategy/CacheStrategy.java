package com.im4j.kakacache.rxjava.netcache.strategy;

import com.im4j.kakacache.rxjava.netcache.ResultData;

import rx.Observable;

/**
 * 缓存策略
 * @version alafighting 2016-06
 */
public interface CacheStrategy<T> {

    public Observable<ResultData<T>> execute(String key, Observable<T> source);

}
