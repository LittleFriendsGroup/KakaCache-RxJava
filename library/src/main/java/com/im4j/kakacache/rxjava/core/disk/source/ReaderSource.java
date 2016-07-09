package com.im4j.kakacache.rxjava.core.disk.source;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @version alafighting 2016-07
 */
public class ReaderSource extends InputStreamReader implements Source {

    private final Source source;

    public ReaderSource(Source source) {
        super(new BasicSource(source));
        this.source = source;
    }

    @Override
    public int read(byte[] buffer, int offset, int byteCount) throws IOException {
        return source.read(buffer, offset, byteCount);
    }
}
