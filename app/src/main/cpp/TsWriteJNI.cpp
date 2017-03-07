#include <jni.h>

//
// Created by wangw on 2017/3/7.
//

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jstring JNICALL
Java_com_wangw_m3u8fortswrite_TsWirte_helloWord(JNIEnv *env, jclass type) {

    return env->NewStringUTF("Hello From JNI");

}

#ifdef __cplusplus
}
#endif