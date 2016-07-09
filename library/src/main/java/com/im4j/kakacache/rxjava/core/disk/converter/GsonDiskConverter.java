package com.im4j.kakacache.rxjava.core.disk.converter;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.im4j.kakacache.rxjava.common.utils.LogUtils;
import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.disk.sink.Sink;
import com.im4j.kakacache.rxjava.core.disk.source.ReaderSource;
import com.im4j.kakacache.rxjava.core.disk.source.Source;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * GSON-数据转换器
 * @version alafighting 2016-07
 */
public class GsonDiskConverter implements IDiskConverter {
    private Gson gson;

    public GsonDiskConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object load(Source source, Type type) {
        Object value = null;
        ReaderSource readerSource = new ReaderSource(source);
        try {
            value = gson.fromJson(readerSource, type);
        } catch (JsonIOException e) {
            LogUtils.log(e);
        } catch (JsonSyntaxException e) {
            LogUtils.log(e);
        } finally {
            Utils.close(readerSource);
        }
        return value;
    }

    @Override
    public void writer(Sink sink, Object data) {
        try {
            String json = gson.toJson(data);
            byte[] bytes = json.getBytes();
            sink.write(bytes, 0, bytes.length);
            sink.flush();
        } catch (JsonIOException e) {
            LogUtils.log(e);
        } catch (JsonSyntaxException e) {
            LogUtils.log(e);
        } catch (IOException e) {
            LogUtils.log(e);
        }
    }

}
