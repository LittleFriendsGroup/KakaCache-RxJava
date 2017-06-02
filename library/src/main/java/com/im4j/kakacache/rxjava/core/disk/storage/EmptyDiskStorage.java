package com.im4j.kakacache.rxjava.core.disk.storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 空的磁盘存储
 * @author imkarl 2016-09
 */
public class EmptyDiskStorage implements IDiskStorage {

    public static final EmptyDiskStorage INSTANCE = new EmptyDiskStorage();

    private EmptyDiskStorage() {
    }

    @Override
    public FileInputStream load(String key) {
        return null;
    }

    @Override
    public FileOutputStream create(String key) {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean remove(String key) {
        return true;
    }

    @Override
    public boolean clear() {
        return true;
    }

    @Override
    public long getTotalSize() {
        return 0;
    }

    @Override
    public long getTotalQuantity() {
        return 0;
    }

}
