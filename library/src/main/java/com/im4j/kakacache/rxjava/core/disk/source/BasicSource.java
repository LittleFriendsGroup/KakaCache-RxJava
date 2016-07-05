package com.im4j.kakacache.rxjava.core.disk.source;

import com.im4j.kakacache.rxjava.common.utils.Utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * 包含基本操作的数据源
 * @version 0.1 king 2016-04
 */
public class BasicSource extends InputStream implements Source {

    volatile Source source;

    public BasicSource(Source source) {
        this.source = source;
    }

    @Override
    public int read() throws IOException {
        return source.read();
    }

    public int read(byte[] buffer, int offset, int byteCount) throws IOException {
        Utils.checkOffsetAndCount(buffer.length, offset, byteCount);
        for (int i = 0; i < byteCount; ++i) {
            int c;
            try {
                if ((c = read()) == -1) {
                    return i == 0 ? -1 : i;
                }
            } catch (IOException e) {
                if (i != 0) {
                    return i;
                }
                throw e;
            }
            buffer[offset + i] = (byte) c;
        }
        return byteCount;
    }

    @Override
    public void close() throws IOException {
        Utils.closeThrowException(source);
    }

}
