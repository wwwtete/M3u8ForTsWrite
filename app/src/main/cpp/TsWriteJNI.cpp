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
//    if (fieldIDs.dataId == NULL) {
//        fieldIDs.dataId = env->GetFieldID(fieldIDs.objClass, "data", "[B");
//    }
//    if (fieldIDs.lengthId == NULL) {
//        fieldIDs.lengthId = env->GetFieldID(fieldIDs.objClass, "length", "I");
//    }
//    if (fieldIDs.sizeId == NULL) {
//        fieldIDs.sizeId = env->GetFieldID(fieldIDs.objClass, "size", "I");
//    }
//    if (fieldIDs.durationId == NULL) {
//        fieldIDs.durationId = env->GetFieldID(fieldIDs.objClass, "duration", "J");
//    }
}
JNIEXPORT void JNICALL
Java_com_wangw_m3u8fortswrite_TsWirte_addH264Data(JNIEnv *env, jclass type, jbyteArray inputBuffer_,
                                                  jint length, jint frameType, jlong ts,
                                                  jobject outPutBuffer) {
//    LOG("[JNI_addH264Data]");
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
Java_com_wangw_m3u8fortswrite_TsWirte_addAACData(JNIEnv *env, jclass type, jbyteArray inputBuffer_,
                                                 jint length, jint samplerate, jint channum,
                                                 jlong ts) {
    uint8_t *inputBuffer = (uint8_t *) env->GetByteArrayElements(inputBuffer_, 0);
    TS.AddAACData(samplerate,channum,inputBuffer,length,(int64_t)ts);
    env->ReleaseByteArrayElements(inputBuffer_, (jbyte *) inputBuffer, 0);
}

JNIEXPORT jstring JNICALL
Java_com_wangw_m3u8fortswrite_TsWirte_helloWord(JNIEnv *env, jclass type) {
    return env->NewStringUTF("Hello From JNI");

}

JNIEXPORT void JNICALL
Java_com_wangw_m3u8fortswrite_TsWirte_close(JNIEnv *env, jclass type, jobject outputBuffer) {

    TS.Close(filebuffer);
//    objClass = env->GetObjectClass(outputBuffer);
//    dataId = env->GetFieldID(objClass,"data","[B");
//    lengthId = env->GetFieldID(objClass,"length","I");
//    sizeId = env->GetFieldID(objClass,"size","I");
//    durationId = env->GetFieldID(objClass,"duration","J");

//    jbyteArray array = env->NewByteArray(filebuffer.ptr);
//    env->SetByteArrayRegion(array, 0, filebuffer.ptr, (const jbyte *) filebuffer.data);
//    env->SetObjectField(outputBuffer, dataId, (jobject) array);
//    env->SetIntField(outputBuffer,lengthId,filebuffer.ptr);
//    env->SetIntField(outputBuffer,sizeId,filebuffer.size);
//    env->SetLongField(outputBuffer,durationId,filebuffer.duration);

    checkFieldID(env,outputBuffer);
    jbyteArray array = env->NewByteArray(filebuffer.ptr);
    env->SetByteArrayRegion(array, 0, filebuffer.ptr, (const jbyte *) filebuffer.data);
    env->SetObjectField(outputBuffer, fieldIDs.dataId, (jobject) array);
    env->SetIntField(outputBuffer,fieldIDs.lengthId,filebuffer.ptr);
    env->SetIntField(outputBuffer,fieldIDs.sizeId,filebuffer.size);
    env->SetLongField(outputBuffer,fieldIDs.durationId,filebuffer.duration);
    env->DeleteLocalRef(array);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
//    LOG("[JNI_OnLoad]");
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

    return result;
}

#ifdef __cplusplus
}
#endif