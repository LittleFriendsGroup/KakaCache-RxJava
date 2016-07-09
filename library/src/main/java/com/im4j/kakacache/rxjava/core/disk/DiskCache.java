package com.im4j.kakacache.rxjava.core.disk;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.utils.TypeToken;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.Cache;
import com.im4j.kakacache.rxjava.core.CacheEntry;
import com.im4j.kakacache.rxjava.core.CacheTarget;
import com.im4j.kakacache.rxjava.core.disk.converter.IDiskConverter;
import com.im4j.kakacache.rxjava.core.disk.journal.IDiskJournal;
import com.im4j.kakacache.rxjava.core.disk.sink.Sink;
import com.im4j.kakacache.rxjava.core.disk.source.Source;
import com.im4j.kakacache.rxjava.core.disk.storage.IDiskStorage;

import java.util.Collection;

/**
 * 磁盘缓存
 * @version 0.1 king 2016-04
 */
public final class DiskCache extends Cache {

    private final IDiskStorage mStorage;
    private final IDiskJournal mJournal;
    private final IDiskConverter mConverter;

    public DiskCache(IDiskStorage storage,
                     IDiskJournal journal,
                     IDiskConverter converter,
                     long maxSize,
                     long maxQuantity) {
        super(maxSize, maxQuantity);
        this.mStorage = storage;
        this.mJournal = journal;
        this.mConverter = converter;
    }


    /**
     * 读取
     * @param key
     * @param <T>
     * @return
     */

    @Override
    protected <T> T doLoad(String key) throws CacheException {
        // 读取缓存
        Source source = mStorage.load(key);
        T value = null;
        if (source != null) {
            value = (T) mConverter.load(source, new TypeToken<T>(){}.getType());
            Utils.close(source);
        }
        return value;
    }

    /**
     * 保存
     * @param maxAge 最大有效期时长（单位：毫秒）
     */
    @Override
    protected <T> void doSave(String key, T value, int maxAge, CacheTarget target) throws CacheException {
        if (target == null || target == CacheTarget.NONE || target == CacheTarget.Memory) {
            return;
        }

        // 写入缓存
        Sink sink = mStorage.create(key);
        if (sink != null) {
            mConverter.writer(sink, value);
            Utils.close(sink);

            mJournal.put(key, new CacheEntry(key, maxAge, target));
        }
    }

    @Override
    protected boolean isExpiry(String key) {
        CacheEntry entry = mJournal.get(key);
        return entry == null || entry.isExpiry();
    }

    @Override
    public boolean containsKey(String key) {
        return mJournal.containsKey(key);
    }

    @Override
    public void remove(String key) throws CacheException {
        mStorage.remove(key);
        mJournal.remove(key);
    }

    @Override
    public void clear() throws CacheException {
        mStorage.clear();
        mJournal.clear();
    }

    @Override
    public Collection<CacheEntry> snapshot() {
        return mJournal.snapshot();
    }

    @Override
    public String getLoseKey() throws CacheException {
        return mJournal.getLoseKey();
    }

    @Override
    public long getTotalSize() {
        long size = mStorage.getTotalSize();
        Utils.checkNotLessThanZero(size);
        return size;
    }

    @Override
    public long getTotalQuantity() {
        long quantity = mStorage.getTotalQuantity();
        Utils.checkNotLessThanZero(quantity);
        return quantity;
    }

}
