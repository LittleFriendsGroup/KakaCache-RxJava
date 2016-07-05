package com.im4j.kakacache.rxjava.netcache;

/**
 * 数据
 * @version alafighting 2016-06
 */
public class ResultData<T> {

    public ResultFrom from;
    public String key;
    public T data;

    public ResultData() {
    }
    public ResultData(ResultFrom from, String key, T data) {
        this.from = from;
        this.key = key;
        this.data = data;
    }

    public ResultFrom getFrom() {
        return from;
    }

    public void setFrom(ResultFrom from) {
        this.from = from;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResultData{" +
                "from=" + from +
                ", key='" + key + '\'' +
                ", data=" + data +
                '}';
    }

}
