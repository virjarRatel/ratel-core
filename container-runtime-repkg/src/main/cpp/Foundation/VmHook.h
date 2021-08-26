//
// Created by alienhe on 2020/12/7.
//

#ifndef RATEL2_VMHOOK_H
#define RATEL2_VMHOOK_H

#endif //RATEL2_VMHOOK_H

#include <jni.h>

void measureNativeOffset(JNIEnv *env, bool isArt);

void
hookMediaDrmGetPropertyByteArrayNative(JNIEnv *env, jobject originJavaMethod, jbyteArray fakeDrmId);

void hookBinder(JNIEnv *env, jclass binderHookManagerClass);

void mark(JNIEnv *env, jclass clazz);

void *getJNIFunction(jmethodID method);