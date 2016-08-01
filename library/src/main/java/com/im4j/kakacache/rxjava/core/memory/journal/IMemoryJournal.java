package com.im4j.kakacache.rxjava.core.memory.journal;

import com.im4j.kakacache.rxjava.core.CacheEntry;

import java.io.Closeable;
import java.util.Collection;

/**
 * 内存缓存日志
 * @version alafighting 2016-04
 */
public interface IMemoryJournal extends Closeable {

    CacheEntry get(String key);

    boolean put(String key, CacheEntry entry);

    boolean containsKey(String key);

    /**
     * 获取准备丢弃的Key
     * @return 准备丢弃的Key（如存储空间不足时，需要清理）
     */
    String getLoseKey();

    boolean remove(String key);

    boolean clear();

    Collection<CacheEntry> snapshot();

}
