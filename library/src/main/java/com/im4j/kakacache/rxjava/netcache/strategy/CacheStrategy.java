package com.im4j.kakacache.rxjava.netcache.strategy;

import com.im4j.kakacache.rxjava.KakaCache;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.netcache.ResultData;
import com.im4j.kakacache.rxjava.netcache.ResultFrom;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 缓存策略
 * @version alafighting 2016-07
 */
public enum CacheStrategy {
    /** 优先缓存 */
    FirstCache{
        @Override
        public <T> Observable<ResultData<T>> execute(String key, Observable<T> source) {
            Observable<ResultData<T>> cache = loadCache(key);
            Observable<ResultData<T>> remote = loadRemote(key, source);
            return Observable.concat(cache, remote)
                    .firstOrDefault(null, it -> it.data != null)
                    .subscribeOn(Schedulers.io());
        }
    },
    /** 优先服务器 */
    FirstRemote{
        @Override
        public <T> Observable<ResultData<T>> execute(String key, Observable<T> source) {
            Observable<ResultData<T>> cache = loadCache(key);
            Observable<ResultData<T>> remote = loadRemote(key, source);
            return Observable.concat(remote, cache)
                    .firstOrDefault(null, it -> it.data != null)
                    .subscribeOn(Schedulers.io());
        }
    },
    /** 先缓存，后网络 */
    CacheAndRemote{
        @Override
        public <T> Observable<ResultData<T>> execute(String key, Observable<T> source) {
            Observable<ResultData<T>> cache = loadCache(key);
            Observable<ResultData<T>> remote = loadRemote(key, source);
            return Observable.concat(cache, remote)
                    .filter(result -> result.data != null)
                    .onBackpressureBuffer()
                    .subscribeOn(Schedulers.io());
        }
    };



    protected <T> Observable<ResultData<T>> loadCache(String key) {
        return KakaCache.manager().load(key).map(it -> {
            LogUtils.debug("loadCache result="+it);
            return new ResultData<>(ResultFrom.Cache, key, (T) it);
        });
    }

    protected <T> Observable<ResultData<T>> loadRemote(String key, Observable<T> source) {
        return source.map(it -> {
            LogUtils.debug("loadRemote result="+it);
            KakaCache.manager().save(key, it).subscribe(status -> LogUtils.log("save status => "+status) );
            return new ResultData<>(ResultFrom.Remote, key, it);
        });
    }

    public abstract <T> Observable<ResultData<T>> execute(String key, Observable<T> source);

}
