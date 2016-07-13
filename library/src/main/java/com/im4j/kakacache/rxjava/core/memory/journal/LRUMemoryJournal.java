package com.im4j.kakacache.rxjava.core.memory.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.exception.NullException;
import com.im4j.kakacache.rxjava.core.CacheEntry;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * LRU缓存日志
 * @version alafighting 2016-07
 */
public class LRUMemoryJournal extends BasicMemoryJournal {

    @Override
    public String getLoseKey() throws CacheException {
        return getKeyValues().entrySet().iterator().next().getKey();
    }

}
