package com.wangw.m3u8fortswrite.recod;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.wangw.m3u8fortswrite.TSFileBuffer;
import com.wangw.m3u8fortswrite.TsFileWrite;
import com.wangw.m3u8fortswrite.TsWirte;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by wangw on 2017/3/7.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class VideoRecordTask implements Runnable {

    //视频相关参数
    private static final String MIME_TYPE = "video/avc";
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int FRAME_RATE = 30;
    private static final int IFRAME_INTERVAL = 0;
    //音频相关参数
    private static final int A_SAMPLE_RATE = 44100;
    private static final int A_SAMPLES_PER_FRAME = 1024;
    private static final int A_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int A_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;


    private int rate=256000;
    private MediaMuxerWrapper mMuxerWrapper;
    private MediaCodec.BufferInfo mVideoBufferInfo;
    //音频Buffer信息
    private MediaCodec.BufferInfo mAudioBufferInfo;
    //视频轨道信息
    private TrackInfo mVideoTrackInfo;
    //音频轨道信息
    private TrackInfo mAudioTrackInfo;
    //视频编码器
    private MediaCodec mVideoEncoder;
    //音频编码器
    private MediaCodec mAudioEncoder;
    boolean eosSentToAudioEncoder = false;
    //视频编码的eos信息
    boolean eosSentToVideoEncoder = false;
    //音频的Eos请求
    boolean audioEosRequested = false;
    //是否所有的停止信息都已收到
    private boolean mStoped;
    private AudioRecord audioRecord;

    private int mWidth = 720;
    private int mHeight = 1280;
    private NV21Convertor mConvertor;

    private Context mContext;
    private byte[] mNowData;
    private boolean mHasNewData;
    private File mOutputFile;
    private int audioInputLength;
    private long audioAbsolutePtsUs;
    private long totalSamplesNum;
    private long startPTS;
    private MediaFormat mVideoOutputFormat;
    private MediaFormat mAudioOutputFormat;
    private int mInverval;
    private long mLastTime;

    private TSFileBuffer mTsBuffer = new TSFileBuffer();

    public VideoRecordTask(Context context,File file) {
        mContext = context;
        this.mOutputFile = file;
        mInverval = 1000/FRAME_RATE;
    }

    @Override
    public void run() {
        try {

            onPrepare();
            audioRecord.startRecording();
            onRecording();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onRecording() {
        while (true){
            if (!mHasNewData)
                continue;
            boolean tempAllStop = mStoped;
            if (tempAllStop){
                sendAudioToEncoder(true);
            }
            if (tempAllStop) {
                sendVideoToEncoder(true);
            }

            if (tempAllStop){
                audioRecord.stop();
            }

            synchronized (mVideoTrackInfo.muxerWrapper.sync){
                drainEncoder(mVideoEncoder,mVideoBufferInfo,mVideoTrackInfo,tempAllStop);
            }

            synchronized (mAudioTrackInfo.muxerWrapper.sync){
                drainEncoder(mAudioEncoder,mAudioBufferInfo,mAudioTrackInfo,tempAllStop);
            }

            long now = System.currentTimeMillis();
            if (!tempAllStop){
                sendAudioToEncoder(false);
                if ((now - mLastTime) >= mInverval) {
                    mLastTime = now;
                    sendVideoToEncoder(false);
                }
            }else {
                break;
            }
        }
    }

    final int TIMEOUT_USEC = 100;
    private void drainEncoder(MediaCodec encoder, MediaCodec.BufferInfo bufferInfo, TrackInfo trackInfo, boolean endOfStream) {
        MediaMuxerWrapper muxerWrapper = trackInfo.muxerWrapper;

        while (true){
            int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo,TIMEOUT_USEC);
//            log("encoderStatus="+encoderStatus);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER){
                if (!endOfStream) {
//                    log("没有可用输出，停止循环,等待下个可用输出");
                    break;
                }else {
                    log("没有可用输出，继续循环等待EOS: "+getStr(encoder));
                }

            }else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
                log("编码器的缓存区发生更改，需要重新获取OutputBuffers");
            }else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                log("编码器的输出格式发生更改 :"+((encoder == mVideoEncoder) ? "Video" : "Audio"));
                if (muxerWrapper.started){
                }else {
                    MediaFormat newFormat = encoder.getOutputFormat();
                    if (encoder == mVideoEncoder) {
                        newFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,IFRAME_INTERVAL);
                        mVideoOutputFormat = newFormat;
                        log("SPS -> "+mVideoOutputFormat.getByteBuffer("csd-0"));
                        log("PPS -> "+mVideoOutputFormat.getByteBuffer("csd-1"));
                        byte[] sps = mVideoOutputFormat.getByteBuffer("csd-0").array();
                        TsWirte.addH264Data(sps,sps.length,TsWirte.FRAMETYPE_SPS,getTs(),mTsBuffer);
                        trackInfo.muxerWrapper.write(mTsBuffer.data);
                        byte[] PPS = mVideoOutputFormat.getByteBuffer("csd-1").array();
                        TsWirte.addH264Data(PPS,PPS.length,TsWirte.FRAMETYPE_PPS,getTs(),mTsBuffer);
                        trackInfo.muxerWrapper.write(mTsBuffer.data);
                    }else if (encoder == mAudioEncoder) {
                        mAudioOutputFormat = newFormat;
                    }
                    trackInfo.index = muxerWrapper.addTrack(newFormat);
                    if (!muxerWrapper.allTracksAdded())
                        break;
                }
            }else if (encoderStatus < 0){
                log("编码器发生未知错误，status = "+encoderStatus);
            }else {
                ByteBuffer outputBuffer = getOutputBuffer(encoder,encoderStatus);
                if (outputBuffer == null) {
                    log("录制失败！！！！！！！！！！！！！！！！");
                    return;
                }
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0){
                    log("编码器配置信息");
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0){
                    if (!muxerWrapper.started){
                        log("合成器还没有开始工作");
                    }else {
//                        outputBuffer.position(bufferInfo.offset);
//                        outputBuffer.limit(bufferInfo.offset+bufferInfo.size);
                        log("合成器写入数据:"+getStr(encoder)+"| lenght="+outputBuffer.limit());
//                        muxerWrapper.mMuxer.writeSampleData(trackInfo.index,outputBuffer,bufferInfo);
                        byte[] data = new byte[bufferInfo.size];
                        outputBuffer.get(data);
                        if (encoder == mVideoEncoder){
                            boolean keyFrame = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
                            Log.d("KEYFRAME", "是否为关键帧: "+keyFrame+" length = "+data.length);
                            Log.d("KEYFRAME2", "是否为关键帧: "+keyFrame+" 4 = "+data[4]);
                            TsWirte.addH264Data(data,data.length,keyFrame ? TsWirte.FRAMETYPE_I : TsWirte.FRAMETYPE_P ,getTs(),mTsBuffer);
                            trackInfo.muxerWrapper.write(mTsBuffer.data);
                            Log.d("KEYFRAME", "输出length: "+mTsBuffer.length+" duration = "+mTsBuffer.duration);
                        }else {
                            TsWirte.addAACData(data,data.length,0,0,getTs());
                        }

                    }
                }
                encoder.releaseOutputBuffer(encoderStatus,false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                    if (!endOfStream){
                        log("输出失败(意外收到EOS)！！！！！！！！！！！！！！！！");
                    }else {
                        log("EOS :"+getStr(encoder));
                        muxerWrapper.finishTrack();
                        if (mStoped){
                            if (encoder == mVideoEncoder){
                                releaseVideoEncoder();
                            }else {
                                releaseAudioEncoder();
                            }
                        }
                    }
                    break;
                }

            }
        }
    }

    private String getStr(MediaCodec encoder){
        return ((encoder == mVideoEncoder) ? "Video" : "Audio");
    }

    private void releaseVideoEncoder() {
        eosSentToVideoEncoder = false;
        if (mVideoEncoder != null){
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
    }

    private void releaseAudioEncoder() {
        eosSentToAudioEncoder = false;
        if (mAudioEncoder != null){
            mAudioEncoder.stop();
            mAudioEncoder.release();
            mAudioEncoder = null;
        }
    }

    byte[] yuv;
    private void sendVideoToEncoder(boolean endOfStream) {
        int inputBufferIndex = mVideoEncoder.dequeueInputBuffer(-1);
        if (inputBufferIndex < 0 && mNowData != null)
            return;
        ByteBuffer buffer = getInputBuffer(mVideoEncoder,inputBufferIndex);
        buffer.clear();
//        yuv = mNowData;
//        if (yuv == null){
//            yuv = new byte[mWidth*mHeight*3/2];
//        }
        yuv = mConvertor.convert(mNowData);//mNowData;//
        buffer.put(yuv);
        audioAbsolutePtsUs = (System.nanoTime())/1000L;
        if (endOfStream){
//            mVideoEncoder.signalEndOfInputStream();
            mVideoEncoder.queueInputBuffer(inputBufferIndex,0,mNowData.length,audioAbsolutePtsUs,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            eosSentToVideoEncoder = true;
        }else {
            log("喂入视频数据:"+yuv.length);
            mVideoEncoder.queueInputBuffer(inputBufferIndex,0,yuv.length,audioAbsolutePtsUs,MediaCodec.BUFFER_FLAG_KEY_FRAME);
        }
    }

    private long mLast = -1;
    public long getTs(){
        if (mLast == -1){
            mLast = (System.nanoTime()/1000L);
            return 0;
        }else {
            long now =  (System.nanoTime()/1000L);
            long spacing = now - mLast;
            mLast = now;
            return spacing;
        }
    }

    private void sendAudioToEncoder(boolean endOfStream) {
        int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0){
            ByteBuffer inputBuffer = getInputBuffer(mAudioEncoder,inputBufferIndex);
            inputBuffer.clear();

            audioInputLength = audioRecord.read(inputBuffer,A_SAMPLES_PER_FRAME*2);
            audioAbsolutePtsUs = (System.nanoTime())/1000L;

            //audioInputLength 除2是因为samples是16bit
            audioAbsolutePtsUs = getJitterFreePTS(audioAbsolutePtsUs,audioInputLength/2);

            if (audioInputLength == AudioRecord.ERROR_INVALID_OPERATION){
                log("无效的音频操作");
            }
            if (audioInputLength == AudioRecord.ERROR_BAD_VALUE){
                log("错误音频数据");
            }
            if (endOfStream){
                log("收到音频EOS 请求");
                mAudioEncoder.queueInputBuffer(inputBufferIndex,0,audioInputLength,audioAbsolutePtsUs,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                eosSentToAudioEncoder = true;
            }else {
                mAudioEncoder.queueInputBuffer(inputBufferIndex,0,audioInputLength,audioAbsolutePtsUs,0);
            }
        }
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

    public ByteBuffer getInputBuffer(MediaCodec codec,int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getInputBuffer(index);
        }else {
            return codec.getInputBuffers()[index];
        }
    }

    public ByteBuffer getOutputBuffer(MediaCodec codec,int index){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return codec.getOutputBuffer(index);
        }else {
            return codec.getOutputBuffers()[index];
        }
    }

    private void onPrepare() throws IOException {
        mStoped = false;
        mVideoBufferInfo = new MediaCodec.BufferInfo();
        EncoderDebugger debugger = EncoderDebugger.debug(mContext,mWidth,mHeight);
        mConvertor = debugger.getNV21Convertor();
        MediaFormat format;
        if (Util.getDgree(mContext) == 0) {
            format = MediaFormat.createVideoFormat("video/avc", mHeight, mWidth);
        } else {
            format = MediaFormat.createVideoFormat("video/avc", mWidth, mHeight);
        }
        format.setInteger(MediaFormat.KEY_BIT_RATE,rate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE,FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,IFRAME_INTERVAL);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, debugger.getEncoderColorFormat());
        mVideoEncoder= MediaCodec.createByCodecName(debugger.getEncoderName());
        mVideoEncoder.configure(format,null,null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mVideoEncoder.start();

        int min_buffer_size = AudioRecord.getMinBufferSize(A_SAMPLE_RATE, A_CHANNEL_CONFIG, A_AUDIO_FORMAT);
        int buffer_size = A_SAMPLES_PER_FRAME * 10;
        if (buffer_size < min_buffer_size)
            buffer_size = ((min_buffer_size / A_SAMPLES_PER_FRAME) + 1) * A_SAMPLES_PER_FRAME * 2;

        //AudioRecord的初始化设置
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,       // source
                A_SAMPLE_RATE,                         // sample rate, hz
                A_CHANNEL_CONFIG,                      // channels
                A_AUDIO_FORMAT,                        // audio format
                buffer_size);                        // buffer size (bytes)

        mAudioBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat mAudioFormat = new MediaFormat();
        mAudioFormat.setString(MediaFormat.KEY_MIME, AUDIO_MIME_TYPE);//音频格式
        mAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);//文件格式
        mAudioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, A_SAMPLE_RATE);//采样率
        mAudioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);//音频录制的渠道数量
        mAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000);//码率
        mAudioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);//音频录制的最大大小
        //音频编码器设置
        mAudioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
        mAudioEncoder.configure(mAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioEncoder.start();


        mMuxerWrapper = new MediaMuxerWrapper(mOutputFile,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mAudioTrackInfo = new TrackInfo();
        mAudioTrackInfo.muxerWrapper = mMuxerWrapper;
        mVideoTrackInfo = new TrackInfo();
        mVideoTrackInfo.muxerWrapper = mMuxerWrapper;
    }

    public void stop(){
        mStoped = true;
    }


    public boolean isStoped() {
        return mStoped;
    }

    public void feedData(byte[] bytes, long time) {
        mNowData = bytes;
        mHasNewData = true;
    }

    private void log(String msg){
        Log.d("RECORDLOG",msg);
    }

    class TrackInfo{
        int index = -1;
        MediaMuxerWrapper muxerWrapper;
    }

    class MediaMuxerWrapper{
        //        MediaMuxer mMuxer;
        private TsFileWrite mWriter;
        final int TOTAL_NUM_TRACKS = 2;
        boolean started = false;
        int numTracksAdded = 0;
        int numTracksFinished = 0;

        Object sync = new Object();

        public MediaMuxerWrapper(File file,int format) {
            restart(file,format);
        }

        private void restart(File file, int format) {
            onStop();
//            mWriter = new TsFileWrite(file);
//            try {
//                mMuxer = new MediaMuxer(file.getAbsolutePath(),format);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        public int addTrack(MediaFormat format){
            numTracksAdded++;
//            int trackIndex = mMuxer.addTrack(format);
            if (allTracksAdded()){
//                mMuxer.start();
                started = true;
            }
            return numTracksAdded;
        }

        public void finishTrack(){
            numTracksFinished++;
            if (allTacksFinished())
                onStop();

        }

        public void write(byte[] data){
//            try {
//                if (data != null && data.length > 0)
//                    mWriter.writeData(data);
//                else {
//                    log("没有可写入的数据");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                log("写入数据失败");
//            }
        }

        public boolean allTracksAdded(){
            return (numTracksAdded == TOTAL_NUM_TRACKS);
        }

        public boolean allTacksFinished(){
            return (numTracksFinished == TOTAL_NUM_TRACKS);
        }


        public void onStop() {
//            if (mMuxer == null)
//                return;
            if (mWriter == null)
                return;
            if (!isStoped())
                return;
            if (!started)
                return;
            try {
//                mMuxer.release();
//                mMuxer = null;
                mWriter.finish();
                started = false;
                numTracksAdded = 0;
                numTracksFinished =0;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
