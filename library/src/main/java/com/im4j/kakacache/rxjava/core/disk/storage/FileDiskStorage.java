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

    /**
     * @param storageDir 磁盘存储根目录
     */
    public FileDiskStorage(File storageDir) {
        if (storageDir == null || !storageDir.isDirectory()) {
            throw new NotFoundException("‘storageDir’ not found.");
        }
        this.mStorageDir = storageDir;
    }

    @Override
    public FileInputStream load(String key) {
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
    public FileOutputStream create(String key) {
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
        // TODO Nothing
    }

    @Override
    public boolean remove(String key) {
        return !Utils.isEmpty(key) && delete(new File(mStorageDir, key));
    }

    @Override
    public boolean clear() {
        try {
            deleteContents(mStorageDir);
            return true;
        } catch (IOException e) {
            return false;
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

    public boolean delete(File file) {
        if (file == null) {
            return false;
        }
        // If delete() fails, make sure it's because the file didn't exist!
        return file.delete() || !file.exists();

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
