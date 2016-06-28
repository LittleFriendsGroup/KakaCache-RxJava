package com.im4j.kakacache.rxjava.core;

/**
 * 缓存目标
 * @version alafighting 2016-04
 */
public enum CacheTarget {

    NONE,
    Memory,
    Disk,
    MemoryDisk;

    public boolean supportMemory() {
        return this==Memory || this==MemoryDisk;
    }

    public boolean supportDisk() {
        return this==Disk || this==MemoryDisk;
    }

}
