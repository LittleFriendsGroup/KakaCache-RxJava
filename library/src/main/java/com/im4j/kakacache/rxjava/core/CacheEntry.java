package com.im4j.kakacache.rxjava.core;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;

import java.io.Serializable;

/**
 * 日志项
 * @version 0.1 king 2016-04
 */
public class CacheEntry implements Serializable {
    public static final String COL_KEY = "key";
    public static final String COL_CREATE_TIME = "create_time";
    public static final String COL_USE_TIME = "use_time";
    public static final String COL_EXPIRY_TIME = "expiry_time";
    public static final String COL_TARGET = "expiry_target";

    /** KEY */
    @PrimaryKey(AssignType.BY_MYSELF)
    @Column(COL_KEY)
    private final String key;
    /** 创建时间 */
    @Column(COL_CREATE_TIME)
    private long createTime;
    /** 最后使用时间 */
    @Column(COL_USE_TIME)
    private long useTime;
    /** 过期时间 */
    @Column(COL_EXPIRY_TIME)
    private long expiryTime;
    /** 缓存目标 */
    @Column(COL_TARGET)
    private CacheTarget target;
    // TODO 有待商讨
//    private long size;

    public CacheEntry(String key, long createTime, long useTime, long expiryTime, CacheTarget target) {
        this.key = key;
        this.createTime = createTime;
        this.useTime = useTime;
        this.expiryTime = expiryTime;
        this.target = target;
    }


    /**
     * 是否过期
     */
    public boolean isExpiry() {
        return System.currentTimeMillis() > expiryTime;
    }


    @Override
    public String toString() {
        return "CacheEntry{" +
                "key='" + key + '\'' +
                ", createTime=" + createTime +
                ", useTime=" + useTime +
                ", expiryTime=" + expiryTime +
                ", target=" + target +
                '}';
    }


    public String getKey() {
        return key;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUseTime() {
        return useTime;
    }

    public void setUseTime(long useTime) {
        this.useTime = useTime;
    }

    public CacheTarget getTarget() {
        return target;
    }

    public void setTarget(CacheTarget target) {
        this.target = target;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

}
