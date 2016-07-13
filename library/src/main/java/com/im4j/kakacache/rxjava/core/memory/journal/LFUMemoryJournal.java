package com.im4j.kakacache.rxjava.core.memory.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.core.CacheEntry;

/**
 * LFU缓存日志
 * @version alafighting 2016-07
 */
public class LFUMemoryJournal extends BasicMemoryJournal {

    @Override
    public String getLoseKey() throws CacheException {
        CacheEntry entry = null;
        for (CacheEntry item : getKeyValues().values()) {
            if (entry == null || entry.getUseCount() > item.getUseCount()) {
                entry = item;
            } else {
                if (entry.getUseCount() == item.getUseCount()
                    && entry.getLastUseTime() > item.getLastUseTime()) {
                    entry = item;
                }
            }
        }
        if (entry != null) {
            return entry.getKey();
        } else {
            return null;
        }
    }

}
