package com.im4j.kakacache.rxjava.core.disk.sink;

import com.im4j.kakacache.rxjava.common.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 包含基本操作的数据槽
 * @version 0.1 king 2016-04
 */
public class BasicSink extends OutputStream implements Sink {

    private volatile Sink sink;

    public BasicSink(Sink sink) {
        this.sink = sink;
    }

    @Override
    public void write(int oneByte) throws IOException {
        sink.write(oneByte);
    }

    @Override
    public void write(byte[] buffer, int offset, int byteCount) throws IOException {
        sink.write(buffer, offset, byteCount);
    }

    @Override
    public void flush() throws IOException {
        sink.flush();
    }

    @Override
    public void close() throws IOException {
        Utils.closeThrowException(sink);
    }

}
