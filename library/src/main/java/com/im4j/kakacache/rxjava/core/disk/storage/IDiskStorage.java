package com.im4j.kakacache.rxjava.core.disk.storage;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 磁盘存储
 * @version alafighting 2016-04
 */
public interface IDiskStorage extends Closeable {

    /**
     * 加载数据源
     * @param key
     * @return
     */
    InputStream load(String key);

    /**
     * 创建数据槽
     * @param key
     */
    OutputStream create(String key);



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
