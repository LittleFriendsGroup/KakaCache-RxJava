package com.im4j.kakacache.rxjava.core.disk.journal;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.exception.NullException;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.CacheEntry;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.WhereBuilder;

import java.io.IOException;
import java.util.Collection;

/**
 * 缓存日志-基类
 * @version alafighting 2016-07
 */
public abstract class BasicDiskJournal implements IDiskJournal {

    private final LiteOrm mLiteOrm;

    public BasicDiskJournal(LiteOrm liteOrm) {
        this.mLiteOrm = liteOrm;
    }

    final LiteOrm getDb() {
        return mLiteOrm;
    }

    @Override
    public CacheEntry get(String key) {
        if (Utils.isEmpty(key)) {
            throw new NullException("key == null");
        }

        CacheEntry entry = mLiteOrm.queryById(key, CacheEntry.class);
        if (entry != null) {
            // 有效期内，才记录最后使用时间
            if (entry.isExpiry()) {
                entry.setLastUseTime(System.currentTimeMillis());
                entry.setUseCount(entry.getUseCount() + 1);
                mLiteOrm.update(entry);
            }
            return entry;
        } else {
            return null;
        }
    }

    @Override
    public final void put(String key, CacheEntry entry) {
        if (Utils.isEmpty(key) || entry == null) {
            throw new NullException("key == null || value == null");
        }
        if (entry.isExpiry()) {
            entry.setLastUseTime(System.currentTimeMillis());
            entry.setUseCount(1);
            mLiteOrm.save(entry);
        } else {
            remove(key);
        }
    }

    @Override
    public final boolean containsKey(String key) {
        CacheEntry entry = get(key);
        return entry != null;
    }

    @Override
    public abstract String getLoseKey() throws CacheException;

    @Override
    public final void remove(String key) {
        mLiteOrm.delete(new WhereBuilder(CacheEntry.class)
                .where(CacheEntry.COL_KEY + " = ?", new String[]{"%"+key+"%"}));
    }

    @Override
    public final void clear() {
        mLiteOrm.deleteAll(CacheEntry.class);
    }

    @Override
    public final Collection<CacheEntry> snapshot() {
        return mLiteOrm.query(CacheEntry.class);
    }

    @Override
    public void close() throws IOException {
        // TODO Nothing
        //mLiteOrm.close();
    }

}
