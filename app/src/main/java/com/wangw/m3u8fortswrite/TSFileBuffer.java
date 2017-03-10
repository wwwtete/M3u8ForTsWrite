package com.wangw.m3u8fortswrite;

/**
 * Created by wangw on 2017/3/7.
 */

public class TSFileBuffer {

    public byte[] data;
    public int length;
    public int size;    //字节
    public long duration;   //

    public int getDuration() {
        return (int) (duration/1000);
    }

    @Override
    public String toString() {
        return "data="+data.length+" | length="+length+" | size="+(size/1024.0f)+"kb | duration="+(duration/1000.0f)+"m";
    }
}
