package com.im4j.kakacache.rxjava;

import android.content.Context;

import com.esotericsoftware.kryo.Kryo;
import com.im4j.kakacache.rxjava.common.utils.L;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.CacheCore;
import com.im4j.kakacache.rxjava.core.disk.converter.KryoDiskConverter;
import com.im4j.kakacache.rxjava.core.disk.journal.LRUDiskJournal;
import com.im4j.kakacache.rxjava.core.disk.storage.EmptyDiskStorage;
import com.im4j.kakacache.rxjava.core.disk.storage.FileDiskStorage;
import com.im4j.kakacache.rxjava.core.memory.journal.LRUMemoryJournal;
import com.im4j.kakacache.rxjava.core.memory.storage.SimpleMemoryStorage;
import com.im4j.kakacache.rxjava.manager.RxCacheManager;
import com.im4j.kakacache.rxjava.netcache.ResultData;
import com.im4j.kakacache.rxjava.netcache.strategy.CacheStrategy;
import com.litesuits.orm.LiteOrm;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

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
    private static File cacheDir;

    public static void init(Context context, File cacheDir) {
        KakaCache.context = context.getApplicationContext();
        KakaCache.cacheDir = cacheDir;
    }

    public static RxCacheManager manager() {
        if (liteOrm == null) {
            liteOrm = LiteOrm.newSingleInstance(context, "kakacache_journal.db");
        }
        liteOrm.setDebugged(true); // open the log

        if (cacheManager == null) {
            File storageDir = cacheDir;
            if (storageDir == null || !storageDir.exists()) {
                storageDir = Utils.getUsableCacheDir(context, DEFAULT_STORAGE_DIR);
            }
            L.log("storageDir="+storageDir);

            CacheCore.Builder coreBuilder = new CacheCore.Builder();
            coreBuilder.memory(new SimpleMemoryStorage());
            coreBuilder.memoryJournal(new LRUMemoryJournal());
            coreBuilder.memoryMax(10 * 1024 * 1024, 1000);
            coreBuilder.disk(EmptyDiskStorage.INSTANCE);

            if (storageDir != null) {
                storageDir.mkdirs();
                try {
                    FileDiskStorage fileDiskStorage = new FileDiskStorage(storageDir);
                    coreBuilder.disk(fileDiskStorage);
                } catch (Exception ignored) { }
            }

            coreBuilder.diskJournal(new LRUDiskJournal(liteOrm));
            coreBuilder.diskMax(30 * 1024 * 1024, 10 * 1000);
            coreBuilder.diskConverter(new KryoDiskConverter(new Kryo()));
            CacheCore core = coreBuilder.create();
            cacheManager = new RxCacheManager(core, DEFAULT_EXPIRES);
        }
        return cacheManager;
    }



    public static <T> ObservableTransformer<T, ResultData<T>> transformer(String key, CacheStrategy strategy) {
        return transformer(key, strategy, DEFAULT_EXPIRES);
    }
    public static <T> ObservableTransformer<T, ResultData<T>> transformer(String key, CacheStrategy strategy, int expires) {
        return new CacheTransformer<T>(key, strategy, expires);
    }

    public static void isDebug(boolean isDebug) {
        L.isDebug = isDebug;
        liteOrm.setDebugged(isDebug);
    }
    public static void setLog(L.Printer printer) {
        L.usePrinter(printer);
    }


    private static class CacheTransformer<T> implements ObservableTransformer<T, ResultData<T>> {
        private String key;
        private CacheStrategy strategy;
        private int expires;

        CacheTransformer(String key, CacheStrategy strategy, int expires) {
            this.key = key;
            this.strategy = strategy;
            this.expires = expires;
        }

        @Override
        public ObservableSource<ResultData<T>> apply(Observable<T> source) {
            return strategy.execute(key, source, expires);
        }
    }

}
