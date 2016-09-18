package com.test.dshell;

/**
 * Created by Liam on 2016/8/30.
 */
public class DShell {
    static {
        System.loadLibrary("dshell");
    }

    public static void init()
    {

    }

    private static native void nativeMark();
}
