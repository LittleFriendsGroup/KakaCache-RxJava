package com.im4j.kakacache.rxjava.core.disk.converter;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * 通用转换器
 *
 * @version alafighting 2016-04
 */
public interface IDiskConverter {

    /**
     * 读取
     */
    Object load(InputStream source, Type type);

    /**
     * 写入
     */
    boolean writer(OutputStream sink, Object data);

}
