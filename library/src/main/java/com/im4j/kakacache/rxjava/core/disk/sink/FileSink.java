package com.im4j.kakacache.rxjava.core.disk.sink;

import com.im4j.kakacache.rxjava.common.exception.NotFoundException;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.common.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件数据槽
 * @version alafighting 2016-06
 */
public class FileSink implements Sink {

    private FileOutputStream outputStream;

    public FileSink(File file) throws NotFoundException {
        try {
            this.outputStream = new FileOutputStream(file, false);
        } catch (FileNotFoundException e) {
            LogUtils.log(e);
            throw new NotFoundException(e);
        }
    }

    @Override
    public void write(int oneByte) throws IOException {
        outputStream.write(oneByte);
    }

    @Override
    public void write(byte[] buffer, int offset, int byteCount) throws IOException {
        outputStream.write(buffer, offset, byteCount);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        Utils.closeThrowException(outputStream);
    }
}
