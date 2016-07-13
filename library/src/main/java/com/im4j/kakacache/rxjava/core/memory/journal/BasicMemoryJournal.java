package com.im4j.kakacache.rxjava.core.memory.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.exception.NullException;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.CacheEntry;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * 缓存日志-基类
 * @version alafighting 2016-07
 */
public abstract class BasicMemoryJournal implements IMemoryJournal {

    private final LinkedHashMap<String, CacheEntry> mKeyValues;

    public BasicMemoryJournal() {
        this.mKeyValues = new LinkedHashMap<>(0, 0.75f, true);
    }

    final LinkedHashMap<String, CacheEntry> getKeyValues() {
        return mKeyValues;
    }

    @Override
    public CacheEntry get(String key) {
        if (Utils.isEmpty(key)) {
            throw new NullException("key == null");
        }

        CacheEntry entry = mKeyValues.get(key);
        if (entry != null) {
            // 有效期内，才记录最后使用时间
            if (entry.isExpiry()) {
                entry.setLastUseTime(System.currentTimeMillis());
                entry.setUseCount(entry.getUseCount() + 1);
            }
            return entry.clone();
        } else {
            return null;
        }
    }

    @Override
    public final void put(String key, CacheEntry entry) {
        if (Utils.isEmpty(key) || entry == null) {
            throw new NullException("key == null || value == null");
        }

        if (entry.isExpiry()) {
            entry.setLastUseTime(System.currentTimeMillis());
            entry.setUseCount(1);
            mKeyValues.put(key, entry);
        } else {
            remove(key);
        }
        mKeyValues.put(key, entry);
    }

    @Override
    public boolean containsKey(String key) {
        if (Utils.isEmpty(key)) {
            throw new NullException("key == null");
        }

        CacheEntry entry = mKeyValues.get(key);
        return entry != null;
    }

    @Override
    public abstract String getLoseKey() throws CacheException;

    @Override
    public void remove(String key) {
        if (Utils.isEmpty(key)) {
            throw new NullException("key == null");
        }

        mKeyValues.remove(key);
    }

    @Override
    public void clear() {
        mKeyValues.clear();
    }

    @Override
    public Collection<CacheEntry> snapshot() {
        return mKeyValues.values();
    }

    @Override
    public void close() throws IOException {
        // TODO Nothing
    }

}
