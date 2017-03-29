package com.wangw.m3u8fortswrite;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.ffmpeg.Mp4DescTools;

public class MainActivity2 extends AppCompatActivity {
    private TextView mTv;
    public static final String mp4filePath = "test.m3u8";
    public static final String outPath = "out_a.mp4";
    public static IjkMediaPlayer ll ;
    private File inFile;
    private File outFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.loadLibrary("ijkffmpeg");
        System.loadLibrary("ijkplayer");
        System.loadLibrary("ijksdl");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mTv = (TextView) findViewById(R.id.mTv);

//        File dir = Environment.getExternalStorageDirectory();
        File dir = new File(Environment.getExternalStorageDirectory()+"/ABC","test");
        inFile = new File(dir, mp4filePath);
        outFile = new File(dir, outPath);

        if (!inFile.exists()) {
            mTv.setText("inFile no exists");
            return;
        }

    }


    public void write(View view) {
        mTv.setText("begin write");
        try {
            Log.e("XXX", "write begin");
         // int code = Mp4DescTools.write(inFile.getAbsolutePath(),"hello", outFile.getAbsolutePath());
            int code = Mp4DescTools.changem3u8Tomp4(inFile.getAbsolutePath(),outFile.getAbsolutePath());
            Log.e("XXX", " write over");
            Log.e("XXX", "code-->" + code);
        } catch (Exception e) {
            Log.e("XXX", "" + e.toString());
        }
        mTv.setText("write over");

    }

    public void read(View view) {
        mTv.setText("begin read");

        try {
            Log.e("XXX", "read begin");
             String desc  = Mp4DescTools.read(outFile.getAbsolutePath());
            Log.e("XXX", "read over");
            Log.e("XXX", "-->" + desc);
        } catch (Exception e) {
            Log.e("XXX", "" + e.toString());
        }

    }
}