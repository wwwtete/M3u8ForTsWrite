/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wangw.m3u8fortswrite.opengl;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.wangw.m3u8fortswrite.TSFileBuffer;
import com.wangw.m3u8fortswrite.TsFileWrite;
import com.wangw.m3u8fortswrite.TsWirte;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class wraps up the core components used for surface-input video encoding.
 * <p>
 * Once created, frames are fed to the input surface.  Remember to provide the presentation
 * time stamp, and always call drainEncoder() before swapBuffers() to ensure that the
 * producer side doesn't get backed up.
 * <p>
 * This class is not thread-safe, with one exception: it is valid to use the input surface
 * on one thread, and drain the output on a different thread.
 */
public class VideoEncoderCore implements Runnable {
    private static final String TAG = "VideoEncoderCore";//MainActivity.TAG;
    private static final boolean VERBOSE = false;

    // TODO: these ought to be configurable as well
    private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 24;               // 30fps
    private static final int IFRAME_INTERVAL = 3;           // 5 seconds between I-frames

    //音频相关参数
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int A_SAMPLE_RATE = 44100;
    private static final int A_SAMPLES_PER_FRAME = 1024;
    private static final int A_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int A_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private Surface mInputSurface;
    private MediaMuxerWrap mMuxerWrap;
    private TrackInfo mVideoTrack;
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;
    private AudioRecord mAudioRecord;
    private TrackInfo mAudioTrack;
    private MediaCodec mAudioEncoder;
    private MediaCodec.BufferInfo mAudioBufferInfo;
    private boolean mRuning = false;
    private boolean mHasData = false;
    private int mAudioInputLength;
    private long mAudioAbsolutePtsUs;
    private long totalSamplesNum;
    private long startPTS;
    private long mStartTime = -1;
    private TSFileBuffer mTsBuffer;


    /**
     * Configures encoder and muxer state, and prepares the input Surface.
     */
    public VideoEncoderCore(int width, int height, int bitRate, File outputFile,String key)
            throws IOException {
        mMuxerWrap = new MediaMuxerWrap(outputFile,key);
        mTsBuffer = new TSFileBuffer();
        initVideoEncoder(width, height, bitRate);
        initAudioRecod();
        initAudioEncoder();
        startRecord();
    }



    private void initAudioEncoder() throws IOException {
        mAudioBufferInfo = new MediaCodec.BufferInfo();
        mAudioTrack = new TrackInfo();
        mAudioTrack.muxerWrap = mMuxerWrap;
        MediaFormat audioFormat = new MediaFormat();
        audioFormat.setString(MediaFormat.KEY_MIME,AUDIO_MIME_TYPE);    //音频格式
        audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE,A_SAMPLE_RATE);  //采样率
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT,1); //音频录制渠道数量
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,MediaCodecInfo.CodecProfileLevel.AACObjectLC);   //文件格式
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE,128000);//码率
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,16384);//音频录制的最大大小

        mAudioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
        mAudioEncoder.configure(audioFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioEncoder.start();
    }

    private void initAudioRecod() {
        int minBufferSize = AudioRecord.getMinBufferSize(A_SAMPLE_RATE,A_CHANNEL_CONFIG,A_AUDIO_FORMAT);
        int bufferSize = A_SAMPLES_PER_FRAME * 10;
        if (bufferSize < minBufferSize){
            bufferSize = ((minBufferSize / A_SAMPLES_PER_FRAME) + 1) * A_SAMPLES_PER_FRAME * 2;
        }

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                A_SAMPLE_RATE,
                A_CHANNEL_CONFIG,
                A_AUDIO_FORMAT,
                bufferSize);
    }

    private void initVideoEncoder(int width, int height, int bitRate) throws IOException {
        mVideoBufferInfo = new MediaCodec.BufferInfo();
        mVideoTrack = new TrackInfo();
        mVideoTrack.muxerWrap = mMuxerWrap;
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);  //视频的颜色格式
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);//视频的码率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);//帧率
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);//关键帧间隔，单位是秒
        if (VERBOSE) Log.d(TAG, "format: " + format);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        mVideoEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mVideoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        //使用Surface来取代输入源，可以直接获取surface展示的数据
        //使用该方法时必须有硬件加速相关APi,比如opengl es
        //如果未配置会造成IllegalStateException
        mInputSurface = mVideoEncoder.createInputSurface();
        mVideoEncoder.start();
    }

    /**
     * Returns the encoder's input surface.
     */
    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void frameAvailable(boolean endOfStream){
        mRuning = !endOfStream;
        mHasData= true;
    }

    private void startRecord(){
        new Thread(this)
                .start();
        mRuning = true;
    }

    @Override
    public void run() {
        boolean endOfStream;
        mAudioRecord.startRecording();
        while (true){
            if (!mHasData){
                continue;
            }
            endOfStream = !mRuning;
            if (endOfStream){
                sendAudioToEncoder(endOfStream);
            }

            drainEncoder(mVideoEncoder,mVideoTrack,mVideoBufferInfo,endOfStream);
            drainEncoder(mAudioEncoder,mAudioTrack,mAudioBufferInfo,endOfStream);

            if (!endOfStream){
                sendAudioToEncoder(endOfStream);
            }else {
                break;
            }
        }
    }

    private void sendAudioToEncoder(boolean endOfStream) {
        int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(-1);

        if (inputBufferIndex >= 0){
            ByteBuffer inputBuffer = getInputBuffer(mAudioEncoder,inputBufferIndex);
            inputBuffer.clear();
            mAudioInputLength = mAudioRecord.read(inputBuffer,A_SAMPLES_PER_FRAME*2);
            mAudioAbsolutePtsUs = (System.nanoTime())/1000L;
//            mAudioAbsolutePtsUs = getJitterFreePTS(mAudioAbsolutePtsUs,mAudioInputLength/2);

            if (mAudioInputLength == AudioRecord.ERROR_BAD_VALUE){
                log("读取音频数据失败:无效的数据");
            }
            if (mAudioInputLength == AudioRecord.ERROR_INVALID_OPERATION){
                log("读取音频数据失败:无效的操作");
            }
            if (endOfStream){
                log("音频发送：EOS 请求");
                mAudioEncoder.queueInputBuffer(inputBufferIndex,0,mAudioInputLength,mAudioAbsolutePtsUs,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }else {
                mAudioEncoder.queueInputBuffer(inputBufferIndex,0,mAudioInputLength,mAudioAbsolutePtsUs,0);
            }
        }
    }

    private long getTs(){
        if (mStartTime == -1){
            mStartTime = System.currentTimeMillis();
        }
        long tst =  (System.currentTimeMillis() - mStartTime);
        Log.d("PTSSS", "getTs ="+tst);
        return tst;
    }

    //音频的pts生成方法
    private long getJitterFreePTS(long bufferPts, long bufferSamplesNum) {
        long correctedPts = 0;
        long bufferDuration = (1000000 * bufferSamplesNum) / (A_SAMPLE_RATE);
        bufferPts -= bufferDuration; // accounts for the delay of acquiring the audio buffer
        if (totalSamplesNum == 0) {
            // reset
            startPTS = bufferPts;
            totalSamplesNum = 0;
        }
        correctedPts = startPTS + (1000000 * totalSamplesNum) / (A_SAMPLE_RATE);
        if (bufferPts - correctedPts >= 2 * bufferDuration) {
            // reset
            startPTS = bufferPts;
            totalSamplesNum = 0;
            correctedPts = startPTS;
        }
        totalSamplesNum += bufferSamplesNum;
        return correctedPts;
    }

    private ByteBuffer getInputBuffer(MediaCodec encoder,int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return encoder.getInputBuffer(index);
        }else {
            return encoder.getInputBuffers()[index];
        }
    }


    /**
     * Extracts all pending data from the encoder and forwards it to the muxer.
     * <p>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     * <p>
     * We're just using the muxer to get a .mp4 file (instead of a raw H.264 stream).  We're
     * not recording audio.
     */
    public void drainEncoder(MediaCodec encoder, TrackInfo trackInfo, MediaCodec.BufferInfo bufferInfo,boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream && encoder == mVideoEncoder) {
            if (VERBOSE) Log.d(TAG, "视频发送: EOS 请求");
            mVideoEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
        while (true) {
            int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
//                    log(getStr(encoder)+"没有可用输出,终止读取");
                    break;      // out of while
                } else {
//                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                    log(getStr(encoder)+"没有可用输出，等待EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = encoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mMuxerWrap.started){

                }else {
                    // should happen before receiving buffers, and should only happen once
                    MediaFormat newFormat = encoder.getOutputFormat();
                    Log.d(TAG, "编码器输出格式更改: " + newFormat+" "+getStr(encoder));
                    if (encoder == mVideoEncoder){
                        log("SPS -> "+newFormat.getByteBuffer("csd-0"));
                        log("PPS -> "+newFormat.getByteBuffer("csd-1"));
                        byte[] sps = newFormat.getByteBuffer("csd-0").array();
                        TsWirte.addH264Data(sps,sps.length,TsWirte.FRAMETYPE_SPS,getTs(),mTsBuffer);
                        byte[] PPS = newFormat.getByteBuffer("csd-1").array();
                        TsWirte.addH264Data(PPS,PPS.length,TsWirte.FRAMETYPE_PPS,getTs(),mTsBuffer);
                    }
                    trackInfo.index = mMuxerWrap.addTrack(newFormat);
                    if (!mMuxerWrap.allAdded())
                        break;
                }
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0) {
                    if (!mMuxerWrap.started) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
//                    encodedData.position(bufferInfo.offset);
//                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    wirteData(encoder, bufferInfo, encodedData);
                    if (VERBOSE) {
                        Log.d(TAG, "sent " + bufferInfo.size + " bytes to muxer, ts=" +
                                bufferInfo.presentationTimeUs);
                    }
                }
                encoder.releaseOutputBuffer(encoderStatus, false);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    mMuxerWrap.finishTrack();
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly "+getStr(encoder));
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached "+ getStr(encoder));
                        if (encoder == mVideoEncoder)
                            stopAndReleaseVideoEncoder();
                        else
                            stopAndReleaseAudioEncoder();
                    }
                    break;      // out of while
                }
            }
        }
    }

    private void wirteData(MediaCodec encoder, MediaCodec.BufferInfo bufferInfo, ByteBuffer encodedData) {
        if (encoder == mVideoEncoder) {
            byte[] data = new byte[bufferInfo.size];
            encodedData.get(data);
            boolean keyFrame = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
            Log.d("KEYFRAME", "是否为关键帧: "+keyFrame+" 4 = "+data[4]);
            TsWirte.addH264Data(data,data.length,keyFrame ? TsWirte.FRAMETYPE_I : TsWirte.FRAMETYPE_P ,getTs(),mTsBuffer);

        }else {
            byte[] data= new byte[bufferInfo.size+7];
            encodedData.get(data,7,bufferInfo.size);
            addADTStoPacket(data,data.length);
            TsWirte.addAACData(data,data.length,0,0,getTs());
//            mMuxerWrap.writeAudio(data);
        }
//                    mMuxerWrap.writeSampleData(trackInfo.index, encodedData, bufferInfo);
    }


    /**
     * 给编码出的aac裸流添加adts头字段
     * @param temp
     * @param length
     */
    private void addADTStoPacket(byte[] temp, int length) {
        int profile = 2;    //AAC LC
        int freqIdx = 4;    //44.1KHz
        int chanCfg = 2;    //CPE
        temp[0] = (byte) 0xFF;
        temp[1] = (byte) 0xF9;
        temp[2] = (byte) (((profile-1)<<6) + (freqIdx<<2) + (chanCfg>>2));
        temp[3] = (byte) (((chanCfg&3)<<6) + (length>>11));
        temp[4] = (byte) ((length&0x7FF)>>3);
        temp[5] = (byte) (((length&7)<<5) + 0x1f);
        temp[6] = (byte) 0xFC;

    }

    private void stopAndReleaseAudioEncoder() {
        if (mAudioEncoder != null){
            mAudioEncoder.stop();
            mAudioEncoder.release();
            mAudioEncoder = null;
        }
    }

    private void stopAndReleaseVideoEncoder() {
        if (mVideoEncoder != null){
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
    }

    private String getStr(MediaCodec encoder){
        return encoder == mVideoEncoder ? "[Video]" : "[Audio]";
    }


    private void log(String msg){
        Log.d(TAG,msg);
    }

    static class TrackInfo{
        int index = -1;
        MediaMuxerWrap muxerWrap;
    }

    class MediaMuxerWrap{
        static final int TOTAL = 2;
        int addTrackTotal;
        int finshTrackTotal;
        //        MediaMuxer muxer;
        boolean started;
        TsFileWrite mTsFileWrite;
//        Object sync = new Object();

        public MediaMuxerWrap(File file,String key) throws IOException {
            onReset(file,key);
        }

        public int addTrack(MediaFormat format){
            addTrackTotal ++;
//            int index = muxer.addTrack(format);
            if (allAdded()){
//                muxer.start();
                started = true;
            }
            return addTrackTotal;
        }

        public void finishTrack(){
            finshTrackTotal++;
            onStop();
        }

        public boolean allAdded(){
            return TOTAL == addTrackTotal;
        }

        public boolean allFinish(){
            return TOTAL == finshTrackTotal;
        }

        private void onReset(File file,String key) throws IOException {
            onStop();
            // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
            // because our MediaFormat doesn't have the Magic Goodies.  These can only be
            // obtained from the encoder after it has started processing data.
            //
            // We're not actually interested in multiplexing audio.  We just want to convert
            // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
//            muxer = new MediaMuxer(file.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mTsFileWrite = new TsFileWrite(file,key);
        }

        private void onStop() {
            if (mTsFileWrite == null || !allFinish() || !started)
                return;

            mTsFileWrite.finish();
            mTsFileWrite = null;
            started = false;
            addTrackTotal =0;
            finshTrackTotal = 0;
        }

        public void writeData(TSFileBuffer buffer){
            if (buffer.data == null || buffer.data.length == 0){
                log("没有可写数据");
                return;
            }
            try {
                Log.d("KEYFRAME", "写入数据: "+buffer.toString());
                mTsFileWrite.writeData(buffer);
            } catch (IOException e) {
                log("写入异常");
                e.printStackTrace();
            }
        }

        public void writeSampleData(int index, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
//            muxer.writeSampleData(index,encodedData,bufferInfo);
        }

        public void writeAudio(byte[] data) {
        }
    }
}

