package com.im4j.kakacache.rxjava.core.memory.journal;

/**
 * LRU缓存日志
 * @version alafighting 2016-07
 */
public class LRUMemoryJournal extends BasicMemoryJournal {

    @Override
    public String getLoseKey() {
        return getKeyValues().entrySet().iterator().next().getKey();
    }

}
