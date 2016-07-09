package com.im4j.kakacache.rxjava.core.disk.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.core.CacheEntry;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.List;

/**
 * LFU缓存日志
 * @version alafighting 2016-07
 */
public class LFUDiskJournal extends BasicDiskJournal {

    public LFUDiskJournal(LiteOrm liteOrm) {
        super(liteOrm);
    }

    @Override
    public String getLoseKey() throws CacheException {
        QueryBuilder query = new QueryBuilder(CacheEntry.class);
        query.orderBy(CacheEntry.COL_USE_COUNT).appendOrderAscBy(CacheEntry.COL_LAST_USE_TIME);
        query.limit(0, 1);
        List<CacheEntry> list = getDb().query(query);
        if (list != null && list.size() >0) {
            return list.get(0).getKey();
        } else {
            return null;
        }
    }

}
