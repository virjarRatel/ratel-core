//
// Created by 邓维佳 on 2019/5/30.
// 相关补丁fix实现，为了避免被反编译，这里的逻辑大多使用jni直接调用。避免在java层容器被发现，以及方便未来在c层面实现流程混淆
//

#ifndef MYAPPLICATION_RATELPATCH_H
#define MYAPPLICATION_RATELPATCH_H

#include <jni.h>

bool ratel_init(JNIEnv *env, jobject context, jstring packageName);

#endif //MYAPPLICATION_RATELPATCH_H
