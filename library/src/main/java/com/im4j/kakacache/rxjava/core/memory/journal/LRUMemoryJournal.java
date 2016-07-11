package com.im4j.kakacache.rxjava.core.memory.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.exception.NullException;
import com.im4j.kakacache.rxjava.core.CacheEntry;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * LRU缓存日志
 * @version alafighting 2016-06
 */
public class LRUMemoryJournal implements IMemoryJournal {

    private final LinkedHashMap<String, CacheEntry> mKeyValue;

    public LRUMemoryJournal() {
        this.mKeyValue = new LinkedHashMap<>(0, 0.75f, true);
    }

    @Override
    public CacheEntry get(String key) {
        if (key == null) {
            throw new NullException("key == null");
        }
        return mKeyValue.get(key);
    }

    @Override
    public void put(String key, CacheEntry entry) {
        if (key == null || entry == null) {
            throw new NullException("key == null || value == null");
        }
        mKeyValue.put(key, entry);
    }

    @Override
    public boolean containsKey(String key) {
        CacheEntry entry = mKeyValue.get(key);
        return entry != null;
    }

    @Override
    public String getLoseKey() throws CacheException {
        return mKeyValue.entrySet().iterator().next().getKey();
    }

    @Override
    public void remove(String key) {
        mKeyValue.remove(key);
    }

    @Override
    public void clear() {
        mKeyValue.clear();
    }

    @Override
    public Collection<CacheEntry> snapshot() {
        return mKeyValue.values();
    }

    @Override
    public void close() throws IOException {
        // TODO Nothing
    }

}
