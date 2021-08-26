//
// Created by liumeng on 2020/10/19.
//

#ifndef ARTXPOSEDNATIVEHOOK_HIDE_H
#define ARTXPOSEDNATIVEHOOK_HIDE_H

#include "android/log.h"

int riru_hide();

#define MTAG "RATELMAP"
#define MLOGD(...) __android_log_print(ANDROID_LOG_DEBUG, MTAG, __VA_ARGS__)
#define MLOGI(...) __android_log_print(ANDROID_LOG_INFO, MTAG, __VA_ARGS__)
#define MLOGW(...) __android_log_print(ANDROID_LOG_WARN, MTAG, __VA_ARGS__)
#define MLOGE(...) __android_log_print(ANDROID_LOG_ERROR, MTAG, __VA_ARGS__)

#endif //ARTXPOSEDNATIVEHOOK_HIDE_H
