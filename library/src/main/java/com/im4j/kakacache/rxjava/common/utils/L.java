package com.im4j.kakacache.rxjava.common.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 日志打印工具
 * @author imkarl 2017-05
 */
public final class L {
    private L() {}

    public interface Printer {
        void print(int level, StackTraceElement element, String tag, Object msg);
    }

    private static class AndroidPrinter implements Printer {
        @Override
        public void print(int level, StackTraceElement element, String tag, Object msg) {
            String className = element.getClassName();
            className = className.substring(className.lastIndexOf(".") + 1);
            String codeLine = className+'.'+element.getMethodName()+'('+element.getFileName()+':'+element.getLineNumber()+')';
            Log.println(level, tag, codeLine);

            String message = toString(msg);
            Log.println(level, tag, "\t" + message);
        }
        private static String toString(Object msg) {
            String message;

            if (msg == null) {
                message = "[null]";
            } else if (msg instanceof Enum) {
                Enum enumObj = (Enum) msg;
                message = enumObj.getClass().getSimpleName()+"."+enumObj.name();
            } else if (msg instanceof Throwable) {
                Throwable tr = (Throwable) msg;
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                tr.printStackTrace(pw);
                pw.flush();
                message = sw.toString();
            } else {
                message = String.valueOf(msg);
            }

            if (TextUtils.isEmpty(message)) {
                message = "[ ]";
            }

            return message;
        }
    }


    public static boolean isDebug = true;
    private final static int DEBUG = Log.DEBUG;
    private final static int INFO = Log.INFO;

    private static Printer mPrinter = new AndroidPrinter();
    private static String mTag = "KakaCache";

    public static synchronized L usePrinter(Printer printer) {
        mPrinter = printer;
        return null;
    }
    public static synchronized L useTag(String tag) {
        mTag = tag;
        return null;
    }

    public static void debug(Object msg) {
        if (isDebug) {
            log(DEBUG, msg);
        }
    }
    public static void log(Object msg) {
        log(INFO, msg);
    }


    private static void log(int level, Object msg) {
        print(level, mTag, msg);
    }
    private static void print(int level, String tag, Object msg) {
        if (mPrinter == null) {
            return;
        }
        StackTraceElement element = new Throwable().getStackTrace()[3];
        mPrinter.print(level, element, tag, msg);
    }

}
