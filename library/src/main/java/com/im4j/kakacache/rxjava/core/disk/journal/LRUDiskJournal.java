package com.im4j.kakacache.rxjava.core.disk.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.exception.NullException;
import com.im4j.kakacache.rxjava.core.CacheEntry;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.WhereBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * LRU缓存日志
 * @version alafighting 2016-07
 */
public class LRUDiskJournal implements IDiskJournal {

    private final LiteOrm mLiteOrm;

    public LRUDiskJournal(LiteOrm liteOrm) {
        this.mLiteOrm = liteOrm;
    }

    @Override
    public CacheEntry get(String key) {
        if (key == null) {
            throw new NullException("key == null");
        }

        CacheEntry entry = mLiteOrm.queryById(key, CacheEntry.class);
        if (entry != null && !entry.isExpiry()) {
            return entry;
        } else {
            return null;
        }
    }

    @Override
    public void put(String key, CacheEntry entry) {
        if (key == null || entry == null) {
            throw new NullException("key == null || value == null");
        }
        mLiteOrm.save(entry);
    }

    @Override
    public boolean containsKey(String key) {
        CacheEntry entry = get(key);
        return entry != null && !entry.isExpiry();
    }

    @Override
    public String getLoseKey() throws CacheException {
        QueryBuilder query = new QueryBuilder(CacheEntry.class);
        query.orderBy(CacheEntry.COL_KEY);
        query.limit(0, 1);
        List<CacheEntry> list = mLiteOrm.query(query);
        if (list != null && list.size() >0) {
            return list.get(0).getKey();
        } else {
            return null;
        }
    }

    @Override
    public void remove(String key) {
        mLiteOrm.delete(new WhereBuilder(CacheEntry.class)
                .where(CacheEntry.COL_KEY + " = ?", new String[]{"%1%"}));
    }

    @Override
    public void clear() {
        mLiteOrm.deleteAll(CacheEntry.class);
    }

    @Override
    public Collection<CacheEntry> snapshot() {
        return mLiteOrm.query(CacheEntry.class);
    }

    @Override
    public void close() throws IOException {
        // TODO Nothing
        //mLiteOrm.close();
    }

}
