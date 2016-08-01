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
    public boolean put(String key, CacheEntry entry) {
        if (Utils.isEmpty(key) || entry == null) {
            throw new NullException("key == null || value == null");
        }

        if (entry.isExpiry()) {
            entry.setLastUseTime(System.currentTimeMillis());
            entry.setUseCount(1);
            return mKeyValues.put(key, entry) != null;
        } else {
            return remove(key);
        }
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
    public abstract String getLoseKey();

    @Override
    public boolean remove(String key) {
        if (Utils.isEmpty(key)) {
            throw new NullException("key == null");
        }

        return mKeyValues.remove(key) != null;
    }

    @Override
    public boolean clear() {
        mKeyValues.clear();
        return true;
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
