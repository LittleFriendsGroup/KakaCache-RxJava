package com.im4j.kakacache.rxjava.core;

import com.im4j.kakacache.rxjava.common.utils.L;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.disk.DiskCache;
import com.im4j.kakacache.rxjava.core.disk.converter.IDiskConverter;
import com.im4j.kakacache.rxjava.core.disk.journal.IDiskJournal;
import com.im4j.kakacache.rxjava.core.disk.storage.IDiskStorage;
import com.im4j.kakacache.rxjava.core.memory.CloneUtils;
import com.im4j.kakacache.rxjava.core.memory.MemoryCache;
import com.im4j.kakacache.rxjava.core.memory.journal.IMemoryJournal;
import com.im4j.kakacache.rxjava.core.memory.storage.IMemoryStorage;

/**
 * 缓存核心
 *
 * @version 0.1 king 2016-04
 */
public class CacheCore {

    private MemoryCache memory;
    private DiskCache disk;

    private CacheCore(MemoryCache memory, DiskCache disk) {
        this.memory = Utils.checkNotNull(memory);
        this.disk = Utils.checkNotNull(disk);
    }


    /**
     * 读取
     */
    public <T> T load(String key) {
        if (memory != null) {
            T result = memory.load(key);
            L.debug("load memory cache key="+key+", value="+result);
            if (result != null) {
                // FIXME 通过标识符判断是否被篡改
                try {
                    return CloneUtils.deepClone(result);
                } catch (Exception e) {
                    L.debug(e);
                }
            }
        }

        if (disk != null) {
            T result = disk.load(key);
            L.debug("load disk cache key="+key+", value="+result);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * 保存
     *
     * @param expires 有效期（单位：毫秒）
     */
    public <T> boolean save(String key, T value, int expires, CacheTarget target) {
        if (value == null) {
            return memory.remove(key) && disk.remove(key);
        }

        if (memory != null) {
            // FIXME 通过标识符判断是否被篡改
            T cloneValue = null;
            try {
                cloneValue = CloneUtils.deepClone(value);
                value = cloneValue;
            } catch (Exception e) {
                L.debug(e);
            }
            memory.save(key, cloneValue, expires, target);
        }
        if (disk != null) {
            return disk.save(key, value, expires, target);
        }

        return false;
    }

    /**
     * 是否包含
     */
    public boolean containsKey(String key) {
        if (memory != null) {
            if (memory.containsKey(key)) {
                return true;
            }
        }
        if (disk != null) {
            if (disk.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除缓存
     */
    public void remove(String key) {
        if (memory != null) {
            memory.remove(key);
        }
        if (disk != null) {
            disk.remove(key);
        }
    }

    /**
     * 清空缓存
     */
    public void clear() {
        if (memory != null) {
            memory.clear();
        }
        if (disk != null) {
            disk.clear();
        }
    }


    /**
     * 构造器
     */
    public static class Builder {
        private IMemoryStorage memory;
        private IMemoryJournal memoryJournal;
        private long memoryMaxSize;
        private long memoryMaxQuantity;

        private IDiskStorage disk;
        private IDiskJournal diskJournal;
        private IDiskConverter diskConverter;
        private long diskMaxSize;
        private long diskMaxQuantity;

        public Builder() {
        }

        public Builder memory(IMemoryStorage memory) {
            this.memory = Utils.checkNotNull(memory);
            return this;
        }

        public Builder memoryJournal(IMemoryJournal journal) {
            this.memoryJournal = Utils.checkNotNull(journal);
            return this;
        }

        public Builder memoryMax(long maxSize, long maxQuantity) {
            this.memoryMaxSize = maxSize;
            this.memoryMaxQuantity = maxQuantity;
            return this;
        }

        public Builder disk(IDiskStorage disk) {
            this.disk = Utils.checkNotNull(disk);
            return this;
        }

        public Builder diskJournal(IDiskJournal journal) {
            this.diskJournal = Utils.checkNotNull(journal);
            return this;
        }

        public Builder diskConverter(IDiskConverter converter) {
            this.diskConverter = Utils.checkNotNull(converter);
            return this;
        }

        public Builder diskMax(long maxSize, long maxQuantity) {
            this.diskMaxSize = maxSize;
            this.diskMaxQuantity = maxQuantity;
            return this;
        }

        public CacheCore create() {
            // TODO 根据配置，选择合适的构造方法
            return new CacheCore(new MemoryCache(memory, memoryJournal, memoryMaxSize, memoryMaxQuantity),
                    new DiskCache(disk, diskJournal, diskConverter, diskMaxSize, diskMaxQuantity));
        }
    }

}
