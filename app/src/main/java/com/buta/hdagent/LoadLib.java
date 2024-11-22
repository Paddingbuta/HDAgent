package com.buta.hdagent;

public class LoadLib {
    static {
        System.loadLibrary("native-lib");
    }

    public static native String getA();

    public static native String getB();

    private static final String CHARSET = "UTF-8";
    public static String INIT_VECTOR;
    public static String SECRET_KEY;

    static {
        INIT_VECTOR = getA();
        SECRET_KEY = getB();
    }
}