package com.im4j.kakacache.rxjava.core;

import com.im4j.kakacache.rxjava.common.utils.Utils;

import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 缓存基类
 * @version alafighting 2016-04
 */
public abstract class BasicCache {

    private final long mMaxSize;
    private final long mMaxQuantity;
    private final ReadWriteLock mLock = new ReentrantReadWriteLock();

    public BasicCache(long maxSize, long maxQuantity) {
        this.mMaxSize = maxSize;
        this.mMaxQuantity = maxQuantity;
    }


    /**
     * 读取
     */
    public final <T> T load(String key) {
        Utils.checkNotNull(key);
        if (!containsKey(key)) {
            return null;
        }

        // 过期自动清理
        if (isExpiry(key)) {
            remove(key);
            return null;
        }

        mLock.readLock().lock();
        try {
            // 读取缓存
            T value = doLoad(key);
            return ensureTypeMatching(value);
        } finally {
            mLock.readLock().unlock();
        }
    }

    /**
     * 确保类型是匹配的
     * @return 如果最终类型和读取的类型不匹配，则返回null
     */
    @SuppressWarnings("unchecked")
    private static <T> T ensureTypeMatching(T value) {
        if (value == null) {
            return null;
        }
        // TODO 检查类型是否匹配，避免出现ClassCastException
        return value;
    }

    /**
     * 读取
     */
    protected abstract <T> T doLoad(String key);

    /**
     * 保存
     * @param maxAge 最大有效期时长（单位：毫秒）
     */
    public final <T> boolean save(String key, T value, int maxAge, CacheTarget target) {
        Utils.checkNotNull(key);

        if (value == null) {
            return remove(key);
        }

        // TODO 先写入，后清理。会超出限定条件，需要一定交换空间
        boolean status = false;
        mLock.writeLock().lock();
        try {
            // 写入缓存
            status = doSave(key, value, maxAge, target);
        } finally {
            mLock.writeLock().unlock();
        }

        // 清理无用数据
        clearUnused();
        return status;
    }

    /**
     * 保存
     * @param maxAge 最长有效期时长（单位：毫秒）
     */
    protected abstract <T> boolean doSave(String key, T value, int maxAge, CacheTarget target);


    /**
     * 是否过期
     */
    protected abstract boolean isExpiry(String key);

    /**
     * 是否包含
     */
    public final boolean containsKey(String key) {
        mLock.readLock().lock();
        try {
            return doContainsKey(key);
        } finally {
            mLock.readLock().unlock();
        }
    }

    /**
     * 删除缓存
     */
    public final boolean remove(String key) {
        mLock.writeLock().lock();
        try {
            return doRemove(key);
        } finally {
            mLock.writeLock().unlock();
        }
    }

    /**
     * 清空缓存
     */
    public final boolean clear() {
        mLock.writeLock().lock();
        try {
            return doClear();
        } finally {
            mLock.writeLock().unlock();
        }
    }

    /**
     * 是否包含
     */
    protected abstract boolean doContainsKey(String key);

    /**
     * 删除缓存
     */
    protected abstract boolean doRemove(String key);

    /**
     * 清空缓存
     */
    protected abstract boolean doClear();


    /**
     * 日志快照
     */
    public abstract Collection<CacheEntry> snapshot();


    /**
     * 获取准备丢弃的Key
     * @return 准备丢弃的Key（如存储空间不足时，需要清理）
     */
    public abstract String getLoseKey();

    /**
     * 缓存大小
     * @return 单位:byte
     */
    public abstract long getTotalSize();

    /**
     * 缓存个数
     * @return 单位:个数
     */
    public abstract long getTotalQuantity();

    /**
     * 清理无用缓存
     */
    public void clearUnused() {
        // 清理过期
        for (CacheEntry entry : snapshot()) {
            if (entry.isExpiry()) {
                remove(entry.getKey());
            }
        }

        // 清理超出缓存
        if (mMaxSize != 0) {
            while (mMaxSize < getTotalSize()) {
                remove(getLoseKey());
            }
        }
        if (mMaxQuantity != 0) {
            while (mMaxQuantity < getTotalQuantity()) {
                remove(getLoseKey());
            }
        }
    }

}
