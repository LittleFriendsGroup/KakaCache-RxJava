package com.im4j.kakacache.rxjava.core.disk.source;

import com.im4j.kakacache.rxjava.common.exception.NotFoundException;
import com.im4j.kakacache.rxjava.common.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 文件数据源
 * @version alafighting 2016-06
 */
public class FileSource implements Source {

    private FileInputStream inputStream;

    public FileSource(File file) throws NotFoundException {
        try {
            this.inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new NotFoundException(e);
        }
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] buffer, int offset, int byteCount) throws IOException {
        return inputStream.read(buffer, offset, byteCount);
    }

    @Override
    public void close() throws IOException {
        Utils.closeThrowException(inputStream);
    }

}
