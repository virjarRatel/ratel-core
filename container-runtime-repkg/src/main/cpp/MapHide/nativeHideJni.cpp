//
// Created by 能链集团 on 2020/11/17.
//
#include "jni.h"
#include "MapsHideHandler.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_virjar_ratel_nativehide_NativeHide_doHide(JNIEnv *env, jclass clazz) {
    doMapsHide(false);
}