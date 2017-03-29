package com.wangw.m3u8fortswrite;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.ffmpeg.Mp4DescTools;

/**
 * Created by wangw on 2017/3/9.
 */

public class MainActivity extends AppCompatActivity  {

    public static final String TAG="OPENGL";
    public static final String CACEHE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ABC";

    private Button mBtn;

    private String mKey="test";
    private boolean mChangeing;
    public static IjkMediaPlayer ll ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        System.loadLibrary("ijkffmpeg");
        System.loadLibrary("ijkplayer");
        System.loadLibrary("ijksdl");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn = (Button) findViewById(R.id.btn_recode);
    }


    public void onClick(View v){
        mKey = System.currentTimeMillis()+"";
        CameraCaptureActivity.jumpTo(this,CACEHE_DIR,mKey);
    }

    public void onChange(View v){
        if (mChangeing){
            Toast.makeText(this,"正在转换中",Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(mKey)){
            Toast.makeText(this,"请先录制后再转换",Toast.LENGTH_LONG).show();
        }else {
//            m3u8ToMp4(new File(CACEHE_DIR+File.separator+mKey,mKey+".m3u8"));
            m3u8ToMp4(new File(CACEHE_DIR+File.separator+mKey,mKey+".m3u8"));
        }
    }

    private void m3u8ToMp4(final File srcFile) {
        if (!srcFile.exists()){
            Toast.makeText(this,"m3u8文件不存在",Toast.LENGTH_LONG).show();
            return;
        }
        mChangeing = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                File outputFile = new File(CACEHE_DIR+File.separator+mKey,"out_a.mp4");
                final int result = Mp4DescTools.changem3u8Tomp4(srcFile.getAbsolutePath(),outputFile.getAbsolutePath());
                mChangeing = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"转换结束,转换结果 = "+result,Toast.LENGTH_LONG).show();
                    }
                });

            }
        }).start();
    }

}

