package com.im4j.kakacache.rxjava.netcache;

import android.util.Log;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 将Observable转换为自动处理缓存的源
 * @version alafighting 2016-06
 */
public class CacheTransformer<T> implements Observable.Transformer<T, CacheTransformer.CacheData<T>> {

    private String key;
    public CacheTransformer(String key) {
        this.key = key;
    }

    @Override
    public Observable<CacheData<T>> call(Observable<T> source) {
        return Observable.concat(
                RxRemoteCache.load(key).map(it -> new CacheData<T>(From.Cache, key, (T)it)),
                source.map(it -> {
                    RxRemoteCache.save(key, it).subscribe(status -> Log.e("save status", "status="+status) );
                    Log.e("transformer", "save  key="+key+", value="+it);
                    return new CacheData<T>(From.Remote, key, it);
                })
        ).first(it -> it.data != null).subscribeOn(Schedulers.io());
    }

    public static final class CacheData<T> {
        public From from;
        public String key;
        public T data;

        public CacheData(From from, String key, T data) {
            this.from = from;
            this.key = key;
            this.data = data;
        }

        @Override
        public String toString() {
            return "CacheData{" +
                    "from=" + from +
                    ", key='" + key + '\'' +
                    ", data=" + data +
                    '}';
        }
    }

    public static enum From {
        Remote, Cache
    }

}
