//
// VirtualApp Native Project
//

#ifndef VIRTUALAPP_LOG_H
#define VIRTUALAPP_LOG_H

#include <android/log.h>

#define TAG "RATEL"
#define MTAG "RATELMAP"

#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG ,__VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG ,__VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG ,__VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG ,__VA_ARGS__)

#define VLOGI(...) __android_log_print(ANDROID_LOG_INFO,"weijia" ,__VA_ARGS__)
#define VLOGE(...) __android_log_print(ANDROID_LOG_ERROR,"weijia" ,__VA_ARGS__)

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define MLOGD(...) __android_log_print(ANDROID_LOG_DEBUG, MTAG, __VA_ARGS__)
#define MLOGI(...) __android_log_print(ANDROID_LOG_INFO, MTAG, __VA_ARGS__)
#define MLOGW(...) __android_log_print(ANDROID_LOG_WARN, MTAG, __VA_ARGS__)
#define MLOGE(...) __android_log_print(ANDROID_LOG_ERROR, MTAG, __VA_ARGS__)

#endif //VIRTUALAPP_LOG_H
