package com.im4j.kakacache.rxjava.core.disk.converter;

import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.disk.sink.BasicSink;
import com.im4j.kakacache.rxjava.core.disk.sink.Sink;
import com.im4j.kakacache.rxjava.core.disk.source.BasicSource;
import com.im4j.kakacache.rxjava.core.disk.source.Source;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

/**
 * 序列化-数据转换器
 * @version alafighting 2016-06
 */
public class SerializableDiskConverter implements IDiskConverter {

    @Override
    public Object load(Source source, Type type) {
        Object value = null;
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(new BasicSource(source));
            value = oin.readObject();
        } catch (IOException e) {
            LogUtils.log(e);
        } catch (ClassNotFoundException e) {
            LogUtils.log(e);
        } finally {
            Utils.close(oin);
        }
        return value;
    }

    @Override
    public void writer(Sink sink, Object data) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new BasicSink(sink));
            oos.writeObject(data);
            oos.flush(); //缓冲流
        } catch (IOException e) {
            LogUtils.log(e);
        } finally {
            Utils.close(oos);
        }
    }

}
