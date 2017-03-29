//
// Created by wangw on 2017/3/10.
//
#include <jni.h>
#include "Log.h"
#include "tswriter.h"

#ifdef __cplusplus
extern "C" {
#endif
struct FieldIDs{
    jclass objClass;
    jfieldID dataId;
    jfieldID lengthId;
    jfieldID sizeId;
    jfieldID durationId;
};
TSWriter TS;
TSFileBuffer filebuffer;
TSWriter::H264FrameType ft;
FieldIDs fieldIDs;

void checkFieldID(JNIEnv *env, jobject pJobject) {
//    LOG("[JNI_checkFieldID]");
    if (fieldIDs.objClass == NULL){
        fieldIDs.objClass = env->GetObjectClass(pJobject);
        fieldIDs.dataId = env->GetFieldID(fieldIDs.objClass, "data", "[B");
        fieldIDs.lengthId = env->GetFieldID(fieldIDs.objClass, "length", "I");
        fieldIDs.sizeId = env->GetFieldID(fieldIDs.objClass, "size", "I");
        fieldIDs.durationId = env->GetFieldID(fieldIDs.objClass, "duration", "J");
    }
}

JNIEXPORT void JNICALL
Java_com_douyaim_qsapp_media_TsMuxer_addH264Data(JNIEnv *env, jclass type, jbyteArray inputBuffer_,
                                                 jint length, jint frameType, jlong ts,
                                                 jobject outPutBuffer) {
    uint8_t *inputBuffer = (uint8_t *) env->GetByteArrayElements(inputBuffer_, 0);



    switch (frameType){
        case 1001:
            ft = TSWriter::H264FrameType::SPS;
            break;
        case 1002:
            ft = TSWriter::H264FrameType::PPS;
            break;
        case 1003:
            ft = TSWriter::H264FrameType::I;
            break;
        case 1004:
            ft = TSWriter::H264FrameType::P;
            break;
    }

    TS.AddH264Data(inputBuffer,length,ft,ts,filebuffer);
    checkFieldID(env,outPutBuffer);
    jbyteArray array = env->NewByteArray(filebuffer.ptr);
    env->SetByteArrayRegion(array, 0, filebuffer.ptr, (const jbyte *) filebuffer.data);
    env->SetObjectField(outPutBuffer, fieldIDs.dataId, (jobject) array);
    env->SetIntField(outPutBuffer,fieldIDs.lengthId,filebuffer.ptr);
    env->SetIntField(outPutBuffer,fieldIDs.sizeId,filebuffer.size);
    env->SetLongField(outPutBuffer,fieldIDs.durationId,filebuffer.duration);

    env->DeleteLocalRef(array);
    env->ReleaseByteArrayElements(inputBuffer_, (jbyte *) inputBuffer, 0);
}

JNIEXPORT void JNICALL
Java_com_douyaim_qsapp_media_TsMuxer_addAACData(JNIEnv *env, jclass type, jbyteArray inputBuffer_,
                                                jint length, jint samplerate, jint channum,
                                                jlong ts) {
    uint8_t *inputBuffer = (uint8_t *) env->GetByteArrayElements(inputBuffer_, 0);
    TS.AddAACData(samplerate,channum,inputBuffer,length,(int64_t)ts);
    env->ReleaseByteArrayElements(inputBuffer_, (jbyte *) inputBuffer, 0);
}

JNIEXPORT void JNICALL
Java_com_douyaim_qsapp_media_TsMuxer_close(JNIEnv *env, jclass type, jobject outputBuffer) {

    TS.Close(filebuffer);
    checkFieldID(env,outputBuffer);
    jbyteArray array = env->NewByteArray(filebuffer.ptr);
    env->SetByteArrayRegion(array, 0, filebuffer.ptr, (const jbyte *) filebuffer.data);
    env->SetObjectField(outputBuffer, fieldIDs.dataId, (jobject) array);
    env->SetIntField(outputBuffer,fieldIDs.lengthId,filebuffer.ptr);
    env->SetIntField(outputBuffer,fieldIDs.sizeId,filebuffer.size);
    env->SetLongField(outputBuffer,fieldIDs.durationId,filebuffer.duration);
    env->DeleteLocalRef(array);
}

#ifdef __cplusplus
}
#endif
