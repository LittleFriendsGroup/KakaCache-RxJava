package com.im4j.kakacache.rxjava.common.exception;

/**
 * 实例化错误
 * @version 0.1 king 2016-07
 */
public class InstanceException extends Exception {

    public InstanceException() {
    }

    public InstanceException(String message) {
        super(message);
    }

    public InstanceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public InstanceException(Throwable throwable) {
        super(throwable);
    }

}
