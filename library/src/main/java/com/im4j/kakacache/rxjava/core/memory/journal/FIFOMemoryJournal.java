package com.im4j.kakacache.rxjava.core.memory.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.core.CacheEntry;

/**
 * FIFO缓存日志
 * @version alafighting 2016-07
 */
public class FIFOMemoryJournal extends BasicMemoryJournal {

    @Override
    public String getLoseKey() throws CacheException {
        CacheEntry entry = null;
        for (CacheEntry item : getKeyValues().values()) {
            if (entry == null || entry.getCreateTime() > item.getCreateTime()) {
                entry = item;
            }
        }
        if (entry != null) {
            return entry.getKey();
        } else {
            return null;
        }
    }

}
