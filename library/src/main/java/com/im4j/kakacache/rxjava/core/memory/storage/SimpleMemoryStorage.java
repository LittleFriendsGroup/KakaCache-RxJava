package com.im4j.kakacache.rxjava.core.memory.storage;

import android.graphics.Bitmap;

import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.common.utils.MemorySizeOf;
import com.im4j.kakacache.rxjava.common.utils.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的内存存储
 * @version alafighting 2016-06
 */
public class SimpleMemoryStorage implements IMemoryStorage {

    private Map<String, Object> mStorageMap;

    public SimpleMemoryStorage() {
        this.mStorageMap = new HashMap<>();
    }

    @Override
    public Object load(String key) {
        if (Utils.isEmpty(key)) {
            return null;
        }
        return mStorageMap.get(key);
    }

    @Override
    public boolean save(String key, Object value) {
        if (Utils.isEmpty(key)) {
            return true;
        }

        return mStorageMap.put(key, value) != null;
    }

    @Override
    public void close() {
        // TODO Nothing
    }

    @Override
    public boolean remove(String key) {
        if (Utils.isEmpty(key)) {
            return true;
        }
        return mStorageMap.remove(key) != null;
    }

    @Override
    public boolean clear() {
        mStorageMap.clear();
        return true;
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
