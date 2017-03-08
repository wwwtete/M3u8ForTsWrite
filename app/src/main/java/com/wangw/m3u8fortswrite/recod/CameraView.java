package com.wangw.m3u8fortswrite.recod;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wangw on 2017/2/8.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    float rate = 1.778f;    ///视频宽高比
    int minPicWidth = 720;
    int minPreviewWidth = 720;

    private Camera mCamera;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Camera.Size mPicSize;
    private Camera.Size mPreSize;
    private FrameCallback mCallback;

    public CameraView(Context context) {
        super(context);
        onInit();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    private void onInit() {
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            onpenCamera();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onpenCamera() throws IOException {
        mCamera = Camera.open(mCameraId);
        if (mCamera == null){
            mCamera = Camera.open();
        }
        if (mCamera == null)
            throw new NullPointerException("获取摄像头");
        Camera.Parameters parameters = mCamera.getParameters();
        mPicSize = getPicSize(parameters.getSupportedPictureSizes());
        mPreSize = getPreviewSize(parameters.getSupportedPreviewSizes());
        parameters.setPictureSize(mPicSize.width,mPreSize.height);
        parameters.setPreviewSize(mPreSize.width,mPreSize.height);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId,cameraInfo);
        int rotate = (360 + cameraInfo.orientation - getDgree()) % 360;
        parameters.setRotation(rotate);
        if (parameters.getMaxNumFocusAreas() > 0){
            Rect rect = new Rect(-50,-50,50,50);
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(rect,1000));
            parameters.setFocusAreas(focusAreas);
        }
        // if the camera support setting of metering area.
        if (parameters.getMaxNumMeteringAreas() > 0){
            List<Camera.Area> meteringAreas = new ArrayList<>();
            Rect areaRect1 = new Rect(-100, -100, 100, 100);
            meteringAreas.add(new Camera.Area(areaRect1, 1000));
            parameters.setMeteringAreas(meteringAreas);
        }
        parameters.setPreviewFormat(ImageFormat.NV21);
        mCamera.setDisplayOrientation((cameraInfo.orientation - getDgree() + 360) % 360);
        mCamera.setParameters(parameters);
        mCamera.setPreviewDisplay(getHolder());
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (mCallback != null)
                mCallback.onFrame(data, System.nanoTime());
            }
        });
        mCamera.startPreview();
    }

    private Camera.Size getPreviewSize(List<Camera.Size> supportedPreviewSizes) {
        Collections.sort(supportedPreviewSizes,sizeComparator);
        int i=0;
        for (Camera.Size size : supportedPreviewSizes) {
            if ((size.height >= minPreviewWidth) && equalRate(size,rate))
                break;
            i++;
        }
        if (i == supportedPreviewSizes.size())
            i =0;
        return supportedPreviewSizes.get(i);

    }

    private Camera.Size getPicSize(List<Camera.Size> supportedPictureSizes) {
        Collections.sort(supportedPictureSizes,sizeComparator);
        int i =0;
        for (Camera.Size size : supportedPictureSizes) {
            if ((size.height >= minPicWidth) && equalRate(size,rate))
                break;
            i++;
        }
        if (i == supportedPictureSizes.size())
            i = 0;
        return supportedPictureSizes.get(i);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    public void setCallback(FrameCallback callback){
        mCallback = callback;
    }

    private static boolean equalRate(Camera.Size s, float rate){
        float r = (float)(s.width)/(float)(s.height);
        if(Math.abs(r - rate) <= 0.03)
        {
            return true;
        }
        else{
            return false;
        }
    }
    private Comparator<Camera.Size> sizeComparator=new Comparator<Camera.Size>(){
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // TODO Auto-generated method stub
            if(lhs.height == rhs.height){
                return 0;
            }
            else if(lhs.height > rhs.height){
                return 1;
            }
            else{
                return -1;
            }
        }
    };

    private int getDgree() {
        int rotation = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        return degrees;
    }
}
