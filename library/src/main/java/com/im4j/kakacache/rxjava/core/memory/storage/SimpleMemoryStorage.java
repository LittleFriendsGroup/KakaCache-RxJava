package com.im4j.kakacache.rxjava.core.memory.storage;

import android.graphics.Bitmap;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.common.utils.MemorySizeOf;
import com.im4j.kakacache.rxjava.common.utils.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的磁盘存储
 * @version alafighting 2016-06
 */
public class SimpleMemoryStorage implements IMemoryStorage {

    private Map<String, Object> mStorageMap;
    private boolean mIsClose = true;

    public SimpleMemoryStorage() {
        this.mStorageMap = new HashMap<>();
        this.mIsClose = false;
    }

    @Override
    public Object load(String key) throws CacheException {
        if (Utils.isEmpty(key)) {
            return null;
        }
        return mStorageMap.get(key);
    }

    @Override
    public void save(String key, Object value) throws CacheException {
        if (Utils.isEmpty(key)) {
            return;
        }

        mStorageMap.put(key, value);
    }

    @Override
    public Map<String, Object> snapshot() {
        return new HashMap<>(mStorageMap);
    }

    @Override
    public void close() {
        this.mIsClose = true;
    }

    @Override
    public boolean isClosed() {
        return mIsClose;
    }

    @Override
    public void remove(String key) throws CacheException {
        if (Utils.isEmpty(key)) {
            return;
        }
        mStorageMap.remove(key);
    }

    @Override
    public void clear() throws CacheException {
        mStorageMap.clear();
    }

    @Override
    public long getTotalSize() {
        long totalSize = 0;
        for (Object value : mStorageMap.values()) {
            totalSize += countSize(value);
        }
        LogUtils.debug(totalSize);
        return totalSize;
    }

    private static long countSize(Object value) {
        if (value == null) {
            return 0;
        }

        // FIXME 更优良的内存大小算法
        long size = 1;
        if (value instanceof MemorySizeOf.SizeOf) {
            LogUtils.debug("SizeOf");
            size = MemorySizeOf.sizeOf((MemorySizeOf.SizeOf) value);
        } else if (value instanceof Bitmap) {
            LogUtils.debug("Bitmap");
            size = MemorySizeOf.sizeOf((Bitmap) value);
        } else if (value instanceof Iterable) {
            LogUtils.debug("Iterable");
            for (Object item : ((Iterable) value)) {
                size += countSize(item);
            }
        } else if (value instanceof Serializable) {
            LogUtils.debug("Serializable");
            size = MemorySizeOf.sizeOf((Serializable) value);
        }
        LogUtils.debug("size="+size+" value="+value);
        return size;
    }

    @Override
    public long getTotalQuantity() {
        return mStorageMap.size();
    }

}
