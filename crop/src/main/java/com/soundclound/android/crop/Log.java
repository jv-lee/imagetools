package com.soundclound.android.crop;

class Log {

    private static final String TAG = "android-crop";

    public static void e(String msg) {
        android.util.Log.e(TAG, msg);
    }

    public static void e(String msg, Throwable e) {
        android.util.Log.e(TAG, msg, e);
    }

    public static void i(String msg) {
        android.util.Log.i(TAG, msg);
    }

    public static void i(String msg, Throwable e) {
        android.util.Log.i(TAG, msg, e);
    }

}
