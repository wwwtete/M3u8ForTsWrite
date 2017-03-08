//
// Created by wangw on 2017/3/7.
//
#include <jni.h>
#include "Log.h"
//#include <string.h>
#include "tswriter.h"

#ifdef __cplusplus
extern "C" {
#endif

TSWriter TS;


JNIEXPORT void JNICALL
Java_com_wangw_m3u8fortswrite_TsWirte_addH264Data(JNIEnv *env, jclass type, jbyteArray inputBuffer_,
                                                  jint length, jint frameType, jlong ts,
                                                  jobject outPutBuffer) {
    uint8_t *inputBuffer = (uint8_t *) env->GetByteArrayElements(inputBuffer_, 0);

    TSWriter::H264FrameType frameType1 = TSWriter::H264FrameType::I;
    TSFileBuffer filebuffer;
    TS.AddH264Data(inputBuffer,length,frameType1,ts,filebuffer);
    jclass objClass = env->GetObjectClass(outPutBuffer);

    jfieldID dataId = env->GetFieldID(objClass,"data","[B");
    jfieldID lengthId = env->GetFieldID(objClass,"length","I");
    jfieldID sizeId = env->GetFieldID(objClass,"size","I");
    jfieldID durationId = env->GetFieldID(objClass,"duration","J");

    TSFileBuffer buffer;
    buffer.data = (uint8_t *) env->GetObjectField(outPutBuffer, dataId);
    buffer.ptr = env->GetIntField(outPutBuffer,lengthId);
    buffer.size = env->GetIntField(outPutBuffer,sizeId);
    buffer.duration = env->GetLongField(outPutBuffer,durationId);


//    env->ReleaseByteArrayElements(inputBuffer_, inputBuffer, 0);
}

JNIEXPORT void JNICALL
Java_com_wangw_m3u8fortswrite_TsWirte_AddAACData(JNIEnv *env, jclass type, jbyteArray inputBuffer_,
                                                 jint length, jint samplerate, jint channum,
                                                 jlong ts) {
    uint8_t *inputBuffer = (uint8_t *) env->GetByteArrayElements(inputBuffer_, 0);
    TS.AddAACData(inputBuffer,length,samplerate,channum,(int64_t)ts);

//    env->ReleaseByteArrayElements(inputBuffer_, inputBuffer, 0);
}

JNIEXPORT jstring JNICALL
Java_com_wangw_m3u8fortswrite_TsWirte_helloWord(JNIEnv *env, jclass type) {

    return env->NewStringUTF("Hello From JNI");

}



#ifdef __cplusplus
}
#endif