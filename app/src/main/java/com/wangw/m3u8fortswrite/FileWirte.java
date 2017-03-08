package com.wangw.m3u8fortswrite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by wangw on 2017/3/8.
 */

public class FileWirte {

    private File mFile;
    private RandomAccessFile mAccessFile;

    public FileWirte(File file) {
        mFile = file;

        try {
            mAccessFile = new RandomAccessFile(mFile,"rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void wirteData(byte[] data) throws IOException {
        mAccessFile.seek(length());
        mAccessFile.write(data);
    }

    public long length(){
        try {
            return mAccessFile.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


}
