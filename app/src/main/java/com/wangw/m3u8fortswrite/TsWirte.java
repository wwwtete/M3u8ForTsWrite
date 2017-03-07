package com.wangw.m3u8fortswrite;

/**
 * Created by wangw on 2017/3/7.
 */

public class TsWirte {

    static {
        System.loadLibrary("tswirte");
    }

    public native static String helloWord();


}
