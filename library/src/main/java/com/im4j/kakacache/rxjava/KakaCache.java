package com.im4j.kakacache.rxjava;

import android.content.Context;

import com.esotericsoftware.kryo.Kryo;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.CacheCore;
import com.im4j.kakacache.rxjava.core.disk.converter.KryoDiskConverter;
import com.im4j.kakacache.rxjava.core.disk.journal.LRUDiskJournal;
import com.im4j.kakacache.rxjava.core.disk.storage.FileDiskStorage;
import com.im4j.kakacache.rxjava.core.memory.journal.LRUMemoryJournal;
import com.im4j.kakacache.rxjava.core.memory.storage.SimpleMemoryStorage;
import com.im4j.kakacache.rxjava.manager.RxCacheManager;
import com.im4j.kakacache.rxjava.netcache.ResultData;
import com.im4j.kakacache.rxjava.netcache.retrofit.KakaRxCallAdapterFactory;
import com.im4j.kakacache.rxjava.netcache.strategy.CacheStrategy;
import com.litesuits.orm.LiteOrm;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * RxJava的远程数据缓存处理
 * @version alafighting 2016-06
 */
public final class KakaCache {

    private KakaCache() {
    }

    // 缓存默认有效期
    private static final int DEFAULT_EXPIRES = 12 * 60 * 60 * 1000;
    // 缓存保存路径
    private static final String DEFAULT_STORAGE_DIR = "kakacache";

    private static LiteOrm liteOrm;
    private static RxCacheManager cacheManager;
    private static Context context;

    public static void init(Context context) {
        KakaCache.context = context.getApplicationContext();
    }

    public static RxCacheManager manager() {
        if (liteOrm == null) {
            liteOrm = LiteOrm.newSingleInstance(context, "kakacache_journal.db");
        }
        liteOrm.setDebugged(true); // open the log

        if (cacheManager == null) {
            File storageDir = new File(Utils.getStorageCacheDir(context), DEFAULT_STORAGE_DIR);
            LogUtils.log("storageDir="+storageDir.getAbsolutePath());

            storageDir.mkdirs();

            CacheCore.Builder coreBuilder = new CacheCore.Builder();
            coreBuilder.memory(new SimpleMemoryStorage());
            coreBuilder.memoryJournal(new LRUMemoryJournal());
            coreBuilder.memoryMax(10 * 1024 * 1024, 1000);
            coreBuilder.disk(new FileDiskStorage(storageDir));
            coreBuilder.diskJournal(new LRUDiskJournal(liteOrm));
            coreBuilder.diskMax(30 * 1024 * 1024, 10 * 1000);
            coreBuilder.diskConverter(new KryoDiskConverter(new Kryo()));
            CacheCore core = coreBuilder.create();
            cacheManager = new RxCacheManager(core, DEFAULT_EXPIRES);
        }
        return cacheManager;
    }



    public static <T> Observable.Transformer<T, ResultData<T>> transformer(String key, CacheStrategy strategy) {
        return new CacheTransformer<T>(key, strategy);
    }

    public static GsonBuilder gson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ResultData.class, new ResultDataAdapter());
        return builder;
    }

    public static Converter.Factory gsonConverter(GsonBuilder builder) {
        builder.registerTypeAdapter(ResultData.class, new ResultDataAdapter());
        return GsonConverterFactory.create(builder.create());
    }
    public static Converter.Factory gsonConverter() {
        return gsonConverter(KakaCache.gson());
    }

    public static CallAdapter.Factory rxCallAdapter() {
        return KakaRxCallAdapterFactory.create();
    }


    private static class CacheTransformer<T> implements Observable.Transformer<T, ResultData<T>> {
        private String key;
        private CacheStrategy strategy;

        public CacheTransformer(String key, CacheStrategy strategy) {
            this.key = key;
            this.strategy = strategy;
        }

        @Override
        public Observable<ResultData<T>> call(Observable<T> source) {
            return strategy.execute(key, source);
        }
    }
    private static class ResultDataAdapter<T> implements JsonSerializer<ResultData<T>>, JsonDeserializer<ResultData<T>> {
        @Override
        public ResultData<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new ResultData(null, null, context.deserialize(json, getWrapType(typeOfT)));
        }
        @Override
        public JsonElement serialize(ResultData<T> src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.data, getWrapType(typeOfSrc));
        }

        private static Type getWrapType(Type typeOf) {
            ParameterizedType type = (ParameterizedType) typeOf;
            return Arrays.asList(type.getActualTypeArguments()).get(0);
        }
    }

}
