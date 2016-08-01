package com.im4j.kakacache.rxjava.core.disk.converter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.im4j.kakacache.rxjava.common.exception.Exception;
import com.im4j.kakacache.rxjava.common.utils.Utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Kryo-数据转换器
 * @version alafighting 2016-07
 */
public class KryoDiskConverter implements IDiskConverter {
    private Kryo kryo;

    public KryoDiskConverter(Kryo kryo) {
        this.kryo = kryo;
    }

    @Override
    public Object load(InputStream source, Type type) {
        Object value = null;
        Input input = null;
        try {
            input = new Input(source);
            value = kryo.readClassAndObject(input);
        } finally {
            Utils.close(input);
        }
        return value;
    }

    @Override
    public boolean writer(OutputStream sink, Object data) {
        Output output = null;
        try {
            output = new Output(sink);
            kryo.writeClassAndObject(output, data);
            return true;
        } catch (Exception ignored) {
            return false;
        } finally {
            Utils.close(output);
        }
    }

}
