package com.im4j.kakacache.rxjava.common.exception;

/**
 * 空数据错误
 * @version 0.1 king 2016-06
 */
public class NullException extends Exception {

    public NullException() {
    }

    public NullException(String message) {
        super(message);
    }

    public NullException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public NullException(Throwable throwable) {
        super(throwable);
    }

}
