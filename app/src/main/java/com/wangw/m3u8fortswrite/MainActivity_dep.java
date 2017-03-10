package com.wangw.m3u8fortswrite;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.wangw.m3u8fortswrite.recod.CameraView;
import com.wangw.m3u8fortswrite.recod.FrameCallback;
import com.wangw.m3u8fortswrite.recod.VideoRecordTask;

import java.io.File;

/**
 * 已废弃，因为这种方式无法控制输出关键帧数量，所以不适用
 */
@Deprecated
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity_dep extends AppCompatActivity implements FrameCallback {

    public static final String TAG="OPENGL";

    private Button mBtnRecode;
    private CameraView mCameraView;

    private VideoRecordTask mRecordTask;
    private Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dep);

        mBtnRecode = (Button) findViewById(R.id.btn_recode);
        mCameraView = (CameraView) findViewById(R.id.camerview);


    }

    public void onClick(View v){

        if (recording()){
            stopRecord();
        }else {
            startRecord();
        }

    }

    private boolean recording() {
        return mRecordTask != null && !mRecordTask.isStoped();
    }


    private void startRecord() {
        mBtnRecode.setText("STOP");
        mCameraView.setCallback(this);
        mRecordTask = new VideoRecordTask(this,getOutPutFile());
        mThread = new Thread(mRecordTask);
        mThread.start();
    }

    public File getOutPutFile(){
        String path = "/sdcard/ABC";
        String name = System.currentTimeMillis()+".ts";
        return new File(path,name);
    }

    private void stopRecord() {
        mRecordTask.stop();
        mBtnRecode.setText("StART");
    }

    @Override
    public void onFrame(byte[] bytes, long time) {
        if (recording()){
            mRecordTask.feedData(bytes,time);
        }
    }
}
