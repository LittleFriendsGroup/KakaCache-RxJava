package com.im4j.kakacache.rxjava.core.disk.storage;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.exception.NotFoundException;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.disk.sink.FileSink;
import com.im4j.kakacache.rxjava.core.disk.sink.Sink;
import com.im4j.kakacache.rxjava.core.disk.source.FileSource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件形式的磁盘存储
 * @version alafighting 2016-06
 */
public class FileDiskStorage implements IDiskStorage {
    private File mStorageDir;
    private boolean mIsClose = true;

    /**
     * @param storageDir 磁盘存储根目录
     */
    public FileDiskStorage(File storageDir) {
        if (storageDir == null || !storageDir.isDirectory()) {
            throw new NotFoundException("‘storageDir’ not found.");
        }
        this.mStorageDir = storageDir;
        this.mIsClose = false;
    }

    @Override
    public FileSource load(String key) throws CacheException {
        if (Utils.isEmpty(key)) {
            return null;
        }
        File file = new File(mStorageDir, key);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        return new FileSource(file);
    }

    @Override
    public Sink create(String key) throws CacheException {
        if (Utils.isEmpty(key)) {
            return null;
        }
        File file = new File(mStorageDir, key);
        if (!exists(file) || file.isDirectory()) {
            try {
                LogUtils.debug("createNewFile => "+file);
                file.createNewFile();
            } catch (IOException e) {
                LogUtils.log(e);
                return null;
            }
        }
        return new FileSink(file);
    }

    @Override
    public Map<String, FileSource> snapshot() {
        Map<String, FileSource> sourceMap = new HashMap<>();

        File[] files = mStorageDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                sourceMap.put(file.getName(), new FileSource(file));
            }
        }

        return sourceMap;
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
        try {
            delete(new File(mStorageDir, key));
        } catch (IOException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void clear() throws CacheException {
        try {
            deleteContents(mStorageDir);
        } catch (IOException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public long getTotalSize() {
        return countSize(mStorageDir);
    }

    @Override
    public long getTotalQuantity() {
        return mStorageDir.list().length;
    }



    public boolean exists(File file) {
        return file != null && file.exists();
    }

    private long countSize(File file) {
        return file.length();
    }

    public void delete(File file) throws IOException {
        if (file == null) {
            return;
        }
        // If delete() fails, make sure it's because the file didn't exist!
        if (!file.delete() && file.exists()) {
            throw new IOException("failed to delete " + file);
        }
    }

    private void deleteContents(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("not a readable directory: " + directory);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete " + file);
            }
        }
    }
}
