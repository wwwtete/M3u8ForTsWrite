package com.wangw.m3u8fortswrite;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.File;

/**
 * Created by wangw on 2017/3/9.
 */

public class MainActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener {

    public static final String TAG="OPENGL";

    static final int FILTER_NONE = 0;

    private GLSurfaceView mGLView;
    private Button mBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mGLView = (GLSurfaceView) findViewById(R.id.cameraPreview_surfaceView);
        mBtn = (Button) findViewById(R.id.btn_recode);


    }


    public void onClick(View v){
        startActivity(new Intent(this,CameraCaptureActivity.class));
    }


    public File getOutPutFile(){
        String path = "/sdcard/ABC";
        String name = System.currentTimeMillis()+".ts";
        return new File(path,name);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
    }

}

