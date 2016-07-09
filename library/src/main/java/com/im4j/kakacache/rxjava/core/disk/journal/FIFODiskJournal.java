package com.im4j.kakacache.rxjava.core.disk.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.exception.NullException;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.CacheEntry;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.WhereBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * FIFO缓存日志
 * @version alafighting 2016-07
 */
public class FIFODiskJournal extends BasicDiskJournal {

    public FIFODiskJournal(LiteOrm liteOrm) {
        super(liteOrm);
    }

    @Override
    public String getLoseKey() throws CacheException {
        QueryBuilder query = new QueryBuilder(CacheEntry.class);
        query.orderBy(CacheEntry.COL_CREATE_TIME);
        query.limit(0, 1);
        List<CacheEntry> list = getDb().query(query);
        if (list != null && list.size() >0) {
            return list.get(0).getKey();
        } else {
            return null;
        }
    }

}
