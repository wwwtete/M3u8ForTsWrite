package com.wangw.m3u8fortswrite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Created by wangw on 2017/3/8.
 */

public class TsFileWrite {

    private File mCacheDir;
//    private RandomAccessFile mAccessFile;
    private int mIndex;
    private String mKey;
    private M3u8Help mM3u8Help;

    public TsFileWrite(File cacheRoot,String key) throws IOException {
        mCacheDir = new File(cacheRoot,key);
        mIndex = 0;
        mKey = key;
        if (!mCacheDir.exists()){
            mCacheDir.mkdirs();
        }
        File m3u8 = new File(mCacheDir,key+".m3u8");
        mM3u8Help = new M3u8Help(m3u8,5);
//        try {
//            mAccessFile = new RandomAccessFile(mCacheDir,"rw");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }


    public void writeData(TSFileBuffer buffer) throws IOException {
        if (buffer.data == null || buffer.size <= 0){
            return;
        }
//        mAccessFile.seek(length());
//        mAccessFile.write(data);
        File file = new File(mCacheDir,mKey+"_"+mIndex+".ts");
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(buffer.data,0,buffer.length);
        stream.flush();
        stream.close();
        Extinfo extinfo = new Extinfo(getProxyUrl(file.getName()), buffer.getDuration());
        mM3u8Help.insert(extinfo);
        mIndex ++;
    }

    public String getProxyUrl(String tsFile) {
        try {
            return String.format(Locale.US, "%s?server=%s", tsFile, URLEncoder.encode("www.test.com","utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public long length(){
//        try {
//            return mAccessFile.length();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }

    public void finish(){
        try {
            mM3u8Help.endlist();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        if (mAccessFile != null){
//            try {
//                mAccessFile.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }


}
