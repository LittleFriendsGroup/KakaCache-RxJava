package com.im4j.kakacache.rxjava.core.memory.storage;

import java.io.Closeable;

/**
 * 内存存储
 * @author alafighting 2016-04
 */
public interface IMemoryStorage extends Closeable {

    /**
     * 读取
     * @param key
     * @return
     */
    Object load(String key);

    /**
     * 保存
     * @param key
     * @param value
     */
    boolean save(String key, Object value);


    /**
     * 关闭
     */
    @Override
    void close();

    /**
     * 删除缓存
     * @param key
     */
    boolean remove(String key);

    /**
     * 清空缓存
     */
    boolean clear();

    /**
     * 缓存总大小
     * @return 单位:byte
     */
    long getTotalSize();

    /**
     * 缓存总数目
     * @return 单位:缓存个数
     */
    long getTotalQuantity();

}
