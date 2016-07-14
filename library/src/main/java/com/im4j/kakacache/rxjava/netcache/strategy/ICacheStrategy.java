package com.im4j.kakacache.rxjava.netcache.strategy;

import com.im4j.kakacache.rxjava.netcache.ResultData;

/**
 * @version alafighting 2016-07
 */
public interface ICacheStrategy {

    <T> rx.Observable<ResultData<T>> execute(String key, rx.Observable<T> source);

}
