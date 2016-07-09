package com.im4j.kakacache.rxjava.core.disk.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.core.CacheEntry;
import com.litesuits.orm.LiteOrm;

/**
 * Unlimited缓存日志
 * @version alafighting 2016-07
 */
public class UnlimitedDiskJournal extends BasicDiskJournal {

    public UnlimitedDiskJournal(LiteOrm liteOrm) {
        super(liteOrm);
    }

    @Override
    public CacheEntry get(String key) {
        // 有效期造假，可以欺骗不被清理
        // cacheEntry.setExpiryTime()
        return super.get(key);
    }

    // 永不清除有效的缓存（过期依旧会被清理）
    @Override
    public String getLoseKey() throws CacheException {
        return null;
    }

}
