package com.im4j.kakacache.rxjava.core.disk.converter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.disk.sink.BasicSink;
import com.im4j.kakacache.rxjava.core.disk.sink.Sink;
import com.im4j.kakacache.rxjava.core.disk.source.BasicSource;
import com.im4j.kakacache.rxjava.core.disk.source.Source;

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
    public Object load(Source source, Type type) {
        Object value = null;
        Input input = null;
        try {
            input = new Input(new BasicSource(source));
            value = kryo.readClassAndObject(input);
        } finally {
            Utils.close(input);
        }
        return value;
    }

    @Override
    public void writer(Sink sink, Object data) {
        Output output = null;
        try {
            output = new Output(new BasicSink(sink));
            kryo.writeClassAndObject(output, data);
        } finally {
            Utils.close(output);
        }
    }

}
