package com.awell.app.utils;

import android.util.Log;

import com.awell.app.BuildConfig;

/**
 * Logging utility
 */
public class LogUtil {
    private static final String TAG = "AwellAutoDsp";

    private static boolean isDebuggable = BuildConfig.DEBUG;

    private static String methodName = null, filename = null, msg = null;
    private static int lineno = 0;

    private LogUtil() {

    }

    public static void i(final String message) {
        if (!isDebuggable) return;

        getMethodNames(new Throwable().getStackTrace());
        Log.i(TAG, createLog(message));
    }

    public static void d(final String message) {
        if (!isDebuggable) return;

        getMethodNames(new Throwable().getStackTrace());
        Log.d(TAG, createLog(message));
    }

    public static void v(final String message) {
        if (!isDebuggable) return;

        getMethodNames(new Throwable().getStackTrace());
        Log.v(TAG, createLog(message));
    }

    public static void e(final String message) {
        if (!isDebuggable) return;

        getMethodNames(new Throwable().getStackTrace());
        Log.e(TAG, createLog(message));
    }

    public static void w(final String message) {
        if (!isDebuggable) return;

        getMethodNames(new Throwable().getStackTrace());
        Log.w(TAG, createLog(message));
    }

    private static void getMethodNames(StackTraceElement[] sElements) {
        filename = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineno = sElements[1].getLineNumber();
    }

    private static String createLog(String log) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(filename);
        buffer.append(" : ");
        buffer.append(methodName);
        buffer.append(" : ");
        buffer.append(lineno);
        buffer.append("]:");
        buffer.append(log);
        return buffer.toString();
    }

}
