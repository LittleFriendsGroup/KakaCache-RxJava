package com.im4j.kakacache.rxjava.common.exception;

/**
 * 没有实现接口的错误
 * @version 0.1 alafighting 2016-07
 */
public class NotImplementException extends Exception {

    public NotImplementException() {
    }

    public NotImplementException(String message) {
        super(message);
    }

    public NotImplementException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public NotImplementException(Throwable throwable) {
        super(throwable);
    }

}
