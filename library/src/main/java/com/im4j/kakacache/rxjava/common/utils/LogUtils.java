package com.im4j.kakacache.rxjava.common.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @version alafighting 2016-07
 */
public final class LogUtils {
    private LogUtils() {
    }

    public static void e(Object obj) {
        StackTraceElement element = new Throwable().getStackTrace()[1];
        String className = element.getClassName();
        className = className.substring(className.lastIndexOf(".") + 1);
        String tag = className+'.'+element.getMethodName()+'('+element.getFileName()+':'+element.getLineNumber()+')';
        String message = toString(obj);

        Log.e("[KakaCache]", tag+"\t"+message);
    }

    private static String toString(Object obj) {
        if (obj == null) {
            return "[null]";
        }
        if (obj instanceof Throwable) {
            return Log.getStackTraceString((Throwable) obj);
        }
        if (obj instanceof Collection) {
            Iterator it = ((Collection) obj).iterator();
            if (! it.hasNext())
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (;;) {
                Object e = it.next();
                sb.append(e);
                if (! it.hasNext())
                    return sb.append(']').toString();
                sb.append(',').append('\n').append(' ');
            }
        }
        return String.valueOf(obj);
    }

}
