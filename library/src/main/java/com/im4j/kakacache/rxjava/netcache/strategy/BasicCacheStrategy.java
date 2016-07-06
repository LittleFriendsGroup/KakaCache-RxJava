package com.im4j.kakacache.rxjava.netcache.strategy;

import com.im4j.kakacache.rxjava.KakaCache;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.netcache.ResultData;
import com.im4j.kakacache.rxjava.netcache.ResultFrom;

import rx.Observable;

/**
 * 缓存策略基类
 * @version alafighting 2016-06
 */
abstract class BasicCacheStrategy implements CacheStrategy {

    <T> Observable<ResultData<T>> loadCache(String key) {
        return KakaCache.manager().load(key).map(it -> {
            LogUtils.debug("loadCache result="+it);
            return new ResultData<>(ResultFrom.Cache, key, (T) it);
        });
    }

    <T> Observable<ResultData<T>> loadRemote(String key, Observable<T> source) {
        return source.map(it -> {
            LogUtils.debug("loadRemote result="+it);
            KakaCache.manager().save(key, it).subscribe(status -> LogUtils.log("save status => "+status) );
            return new ResultData<>(ResultFrom.Remote, key, it);
        });
    }

}
