package com.im4j.kakacache.rxjava.core.disk.storage;

import com.im4j.kakacache.rxjava.common.exception.CacheException;
import com.im4j.kakacache.rxjava.common.exception.NotFoundException;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.common.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
    public FileInputStream load(String key) throws CacheException {
        if (Utils.isEmpty(key)) {
            return null;
        }
        File file = new File(mStorageDir, key);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new NotFoundException(e);
        }
    }

    @Override
    public FileOutputStream create(String key) throws CacheException {
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
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new NotFoundException(e);
        }
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
