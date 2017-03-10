package com.wangw.m3u8fortswrite;

/**
 * Created by wangw on 2017/2/28.
 */

public class Extinfo {

    public int duration;
    public String fileName;

    public Extinfo() {
    }

    public Extinfo(String fileName, int duration) {
        this.fileName = fileName;
        this.duration = duration;
    }
}
