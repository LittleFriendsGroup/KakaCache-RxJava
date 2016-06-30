package com.im4j.kakacache.rxjava.netcache.strategy;

import android.util.Log;

import com.im4j.kakacache.rxjava.netcache.ResultData;
import com.im4j.kakacache.rxjava.netcache.ResultFrom;
import com.im4j.kakacache.rxjava.netcache.RxRemoteCache;

import rx.Observable;

/**
 * 缓存策略基类
 * @version alafighting 2016-06
 */
abstract class BasicCacheStrategy<T> implements CacheStrategy {

    Observable<ResultData<T>> loadCache(String key) {
        return RxRemoteCache.load(key).map(it -> new ResultData<>(ResultFrom.Cache, key, (T) it));
    }

    Observable<ResultData<T>> loadRemote(String key, Observable<T> source) {
        return source.map(it -> {
            RxRemoteCache.save(key, it).subscribe(status -> Log.e("save status", "status="+status) );
            Log.e("transformer", "save  key="+key+", value="+it);
            return new ResultData<T>(ResultFrom.Remote, key, it);
        });
    }

}
