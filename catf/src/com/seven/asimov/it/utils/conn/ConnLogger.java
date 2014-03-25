package com.seven.asimov.it.utils.conn;

import android.util.Log;

/**
 * Simple Logger Adater for Connectivity framework.
 * If we want to run this framework in other environment,
 * just need to rewrite this class.
 *
 * @author wli
 */

public class ConnLogger {

    public static void debug(String tag, String message) {
        Log.d(tag, addThreadName(message));
    }

    public static void error(String tag, String message) {
        Log.e(tag, addThreadName(message));
    }

    public static void info(String tag, String message) {
        Log.i(tag, addThreadName(message));
    }

    public static void trace(String tag, String message) {
        Log.v(tag, addThreadName(message));
    }

    public static void warn(String tag, String message) {
        Log.w(tag, addThreadName(message));
    }

    public static void debug(String tag, String message, Throwable t) {
        Log.d(tag, addThreadName(message), t);
    }

    public static void error(String tag, String message, Throwable t) {
        Log.e(tag, addThreadName(message), t);
    }

    public static void info(String tag, String message, Throwable t) {
        Log.i(tag, addThreadName(message), t);
    }

    public static void trace(String tag, String message, Throwable t) {
        Log.v(tag, addThreadName(message), t);
    }

    public static void warn(String tag, String message, Throwable t) {
        Log.w(tag, addThreadName(message), t);
    }

    private static String addThreadName(String message) {
        return "[" + Thread.currentThread().getName() + "] " + message;
    }
}
