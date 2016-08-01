package com.im4j.kakacache.rxjava.core.disk.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.litesuits.orm.LiteOrm;

/**
 * Unlimited缓存日志
 * @version alafighting 2016-07
 */
public class UnlimitedDiskJournal extends BasicDiskJournal {

    public UnlimitedDiskJournal(LiteOrm liteOrm) {
        super(liteOrm);
    }

    // 永不清除有效的缓存（过期依旧会被清理）
    @Override
    public String getLoseKey() {
        return null;
    }

}
