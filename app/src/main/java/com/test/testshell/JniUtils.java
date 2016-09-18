package com.test.testshell;

/**
 * Created by Liam on 2016/6/7.
 */
public class JniUtils {

    public native String getCLanguageString();

    static {
        System.loadLibrary("jnitest");   //defaultConfig.ndk.moduleName
    }
}
