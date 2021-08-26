//
// VirtualApp Native Project
//

#ifndef NDK_CORE_H
#define NDK_CORE_H

#include <jni.h>
#include <stdlib.h>


#include "Helper.h"
#include "../Foundation/IORelocator.h"



//com.virjar.ratel.NativeBridge
#define RATEL_NATIVE_BRIDGE_CLASS_NAME "com/virjar/ratel/NativeBridge"

extern jclass nativeEngineClass;
extern jclass nativeBridgeClass;
extern JavaVM *vm;

extern char *artMethodSizeTestCLass;
extern char *sandHookMethodResolverClass;
extern char *sandClassNeverCallClass;
extern char *sandSandHookClass;
extern char *sandHookConfigClass;
extern char *sandHookPendingHookHandlerClass;
extern char *propertiesMockItemClass;
extern char *selfExplosionClass;


extern int SDK_INT;
extern bool HOST_DEBUG;
extern bool RATEL_BUILD_DEBUG;
extern bool USE_INLINE_HOOK_DOBBY;

extern bool IS_EXECVE_SUB_PROCESS;

extern bool enable_zelda;
extern char *zelda_origin_package_name;
extern char *zelda_now_package_name;
extern size_t zelda_now_package_name_length;
extern size_t zelda_origin_package_name_length;

extern int user_seed;
extern bool open_mac_address_fake;

JNIEnv *getEnv();

JNIEnv *ensureEnvCreated();

void DetachCurrentThread();

extern int64_t mock_memory_diff;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved);

bool registerIOMethod(JNIEnv *env, const char *ratelNativeClass);

bool registerSandHookNativeMethod(JNIEnv *env, const char *ratelNativeClass);

bool
registerSandHookClassNevelCallMethod(JNIEnv *env, const char *sandHookClassNeverCallClassChars);


#endif //NDK_CORE_H
