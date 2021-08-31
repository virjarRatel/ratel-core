//
// Created by 邓维佳 on 2019-07-29.
//
#include <jni.h>
#include <Foundation/Log.h>
#include <Foundation/IORelocator.h>
#include "Foundation/SandboxProperties.h"
#include <unistd.h>
#include <sys/wait.h>
#include <Foundation/RatelPatch.h>
#include <Unpacker.h>
#include <sys/mman.h>
#include "VAJni.h"
#include "Log.h"
#include "MapsHideHandler.h"
#include "NativeStackTracer.h"
#include "VmHook.h"

jclass nativeEngineClass;
int64_t mock_memory_diff = 0;

bool enable_zelda = false;
char *zelda_origin_package_name;
char *zelda_now_package_name;
size_t zelda_now_package_name_length;
size_t zelda_origin_package_name_length;

extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_enableUnpackComponent(JNIEnv *env, jclass clazz,
                                                        jstring dump_dir, jboolean needDumpMethod) {
    ScopeUtfString scopeDumpDir(dump_dir);
    UnpackerInstance.init(std::string(scopeDumpDir.c_str()), needDumpMethod);
}


extern "C"
jobject JNICALL
Java_com_virjar_ratel_RatelNative_methodDex(JNIEnv *env, jclass clazz,
                                            jobject methodIdObject) {
    jmethodID methodId = env->FromReflectedMethod(methodIdObject);
    return UnpackerInstance.methodDex(env, methodId);
}

extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_nativeEnableIORedirect
        (JNIEnv *env, jclass, jint apiLevel,
         jint preview_api_level, jstring soPath, jstring soPath64) {
    ScopeUtfString so_path(soPath);
    ScopeUtfString so_path_64(soPath64);
    IOUniformer::startUniformer(apiLevel, preview_api_level, so_path.c_str(), so_path_64.c_str());
}

extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_nativeIOWhitelist
        (JNIEnv *env, jclass jclazz, jstring _path) {
    ScopeUtfString path(_path);
    IOUniformer::whitelist(path.c_str());
}

extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_nativeIOForbid
        (JNIEnv *env, jclass jclazz, jstring _path) {
    ScopeUtfString path(_path);
    IOUniformer::forbid(path.c_str());
}

extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_nativeIOReadOnly
        (JNIEnv *env, jclass jclazz, jstring _path) {
    ScopeUtfString path(_path);
    IOUniformer::readOnly(path.c_str());
}

extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_nativeIORedirect
        (JNIEnv *env, jclass jclazz, jstring origPath, jstring newPath) {
    ScopeUtfString orig_path(origPath);
    ScopeUtfString new_path(newPath);
    IOUniformer::relocate(orig_path.c_str(), new_path.c_str());

}
extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_clearIORedirectConfig
        (JNIEnv *env, jclass jclazz) {
    IOUniformer::clearRelocateConfig();
}

extern "C"
jstring JNICALL
Java_com_virjar_ratel_RatelNative_getRedirectedPath
        (JNIEnv *env, jclass jclazz, jstring origPath) {
    ScopeUtfString orig_path(origPath);
    char buffer[PATH_MAX];
    const char *redirected_path = IOUniformer::query(orig_path.c_str(), buffer, sizeof(buffer));
    if (redirected_path != nullptr) {
        return env->NewStringUTF(redirected_path);
    }
    return nullptr;
}

extern "C"
jstring JNICALL
Java_com_virjar_ratel_RatelNative_nativeReverseRedirectedPath
        (JNIEnv *env, jclass jclazz, jstring redirectedPath) {
    ScopeUtfString redirected_path(redirectedPath);
    char buffer[PATH_MAX];
    const char *orig_path = IOUniformer::reverse(redirected_path.c_str(), buffer, sizeof(buffer));
    return env->NewStringUTF(orig_path);
}


extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_nativeTraceProcess(JNIEnv *env, jclass jclazz, jint sdkVersion) {

}


extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_nativeAddMockSystemProperty
        (JNIEnv *env, jclass jclazz, jstring key, jstring value) {
    if (key == nullptr || value == nullptr) {
        jclass newExcCls = env->FindClass("java/lang/NullPointerException");
        env->ThrowNew(newExcCls, "key and value can not be null");
        return;
    }
    ScopeUtfString properties_key(key);
    ScopeUtfString properties_value(value);
    add_mock_properties(properties_key.c_str(), properties_value.c_str());
}

extern "C"
jobject  JNICALL
Java_com_virjar_ratel_RatelNative_nativeQueryMockSystemProperty(JNIEnv *env, jclass jclazz,
                                                                jstring key) {
    ScopeUtfString properties_key(key);

    PropertiesMockItem *mock_properties_value = query_mock_properties(properties_key.c_str());
    if (mock_properties_value == nullptr) {
        return nullptr;
    }
    jclass retClass = env->FindClass(propertiesMockItemClass);
    jmethodID constructorMethodId = env->GetMethodID(retClass, "<init>",
                                                     "(Ljava/lang/String;Ljava/lang/String;)V");

    jstring value = nullptr;
    if (mock_properties_value->properties_value) {
        value = env->NewStringUTF(mock_properties_value->properties_value);
    }

    return env->NewObject(retClass, constructorMethodId, key, value);
}

extern "C"
jboolean JNICALL
Java_com_virjar_ratel_RatelNative_nativeInit(JNIEnv *env, jclass jclazz, jobject context,
                                             jstring packageName) {
    return static_cast<jboolean>(ratel_init(env, context, packageName));
}

//public static native  void setMemoryDiff(long memoryDiff);
extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_setMemoryDiff(JNIEnv *env, jclass jclazz, jlong memory_diff) {
    mock_memory_diff = memory_diff;
}

//public static native void setupUserSeed(long userSeed);
extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_setupUserSeed(JNIEnv *env, jclass jclazz, jint userSeed) {
    user_seed = userSeed;
    char api_level_chars[56];
    sprintf(api_level_chars, "%i", userSeed);
    setenv("FP_USER_SEED", api_level_chars, 1);
}

//public static native void enableZeldaNative(String originPackageName,String nowPackageName);
extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_enableZeldaNative(JNIEnv *env, jclass jclazz,
                                                    jstring originPackageName,
                                                    jstring nowPackageName) {
    zelda_origin_package_name = strdup(ScopeUtfString(originPackageName).c_str());
    zelda_now_package_name = strdup(ScopeUtfString(nowPackageName).c_str());

    zelda_origin_package_name_length = strlen(zelda_origin_package_name);
    zelda_now_package_name_length = strlen(zelda_now_package_name);
    enable_zelda = true;
}


// public static native void enableMacAddressFake();
extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_enableMacAddressFake(JNIEnv *env, jclass jclazz) {
    open_mac_address_fake = true;
}

// public static native void traceFilePath(String path);
extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_traceFilePath(JNIEnv *env, jclass jclazz, jstring path) {
    ScopeUtfString scopeUtfString(path);
    add_io_trace_config(scopeUtfString.c_str());
}


extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_hideMaps(JNIEnv *env, jclass clazz) {
    doMapsHide(true);
}

extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_mockPathStat(JNIEnv *env, jclass clazz, jstring path,
                                               jlong last_modified) {
    ScopeUtfString scopeUtfString(path);
    IOUniformer::addMockStat(scopeUtfString.c_str(), last_modified);
}

extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_fakeMediaDrmId(JNIEnv *env, jclass jclazz,
                                                 jobject originJavaMethod,
                                                 jbyteArray fakeDrmId) {
    hookMediaDrmGetPropertyByteArrayNative(env, originJavaMethod, fakeDrmId);
}

extern "C"
void JNICALL
Java_com_virjar_ratel_RatelNative_hookBinder(JNIEnv *env, jclass jclazz,
                                             jclass binderHookManagerClass) {
    hookBinder(env, binderHookManagerClass);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_virjar_ratel_RatelNative_methodNativePtr(JNIEnv *env, jclass jclazz, jobject method) {
    if (method == nullptr) {
        return 0;
    }
    jmethodID methodId = env->FromReflectedMethod(method);
    return (jlong) getJNIFunction(methodId);
}


// public static native ByteBuffer dumpMemory(long start, long size);
extern "C"
JNIEXPORT jobject JNICALL
Java_com_virjar_ratel_RatelNative_dumpMemory(JNIEnv *env, jclass jclazz, jlong start, jlong size) {
    mprotect((void *) start, size, PROT_READ | PROT_WRITE | PROT_EXEC);
    return env->NewDirectByteBuffer((void *) start, size);
}

bool registerIOMethod(JNIEnv *env, const char *ratelNativeClass) {
    nativeEngineClass = (jclass) env->NewGlobalRef(env->FindClass(ratelNativeClass));
    if (nativeEngineClass == nullptr) {
        return false;
    }

    char nativeQueryMockSystemPropertiesMethodSign[512] = "(Ljava/lang/String;)L";
    strcat(nativeQueryMockSystemPropertiesMethodSign, propertiesMockItemClass);
    strcat(nativeQueryMockSystemPropertiesMethodSign, ";");
    static JNINativeMethod methods[] = {
            {"nativeReverseRedirectedPath",   "(Ljava/lang/String;)Ljava/lang/String;",            (void *) Java_com_virjar_ratel_RatelNative_nativeReverseRedirectedPath},
            {"nativeGetRedirectedPath",       "(Ljava/lang/String;)Ljava/lang/String;",            (void *) Java_com_virjar_ratel_RatelNative_getRedirectedPath},
            {"nativeIORedirect",              "(Ljava/lang/String;Ljava/lang/String;)V",           (void *) Java_com_virjar_ratel_RatelNative_nativeIORedirect},
            {"nativeIOWhitelist",             "(Ljava/lang/String;)V",                             (void *) Java_com_virjar_ratel_RatelNative_nativeIOWhitelist},
            {"nativeIOForbid",                "(Ljava/lang/String;)V",                             (void *) Java_com_virjar_ratel_RatelNative_nativeIOForbid},
            {"nativeIOReadOnly",              "(Ljava/lang/String;)V",                             (void *) Java_com_virjar_ratel_RatelNative_nativeIOReadOnly},
            {"nativeEnableIORedirect",        "(IILjava/lang/String;Ljava/lang/String;)V",         (void *) Java_com_virjar_ratel_RatelNative_nativeEnableIORedirect},
            {"nativeAddMockSystemProperty",   "(Ljava/lang/String;Ljava/lang/String;)V",           (void *) Java_com_virjar_ratel_RatelNative_nativeAddMockSystemProperty},
            {"nativeTraceProcess",            "(I)V",                                              (void *) Java_com_virjar_ratel_RatelNative_nativeTraceProcess},
            {"nativeQueryMockSystemProperty", nativeQueryMockSystemPropertiesMethodSign,           (void *) Java_com_virjar_ratel_RatelNative_nativeQueryMockSystemProperty},
            {"nativeInit",                    "(Landroid/content/Context;Ljava/lang/String;)Z",    (void *) Java_com_virjar_ratel_RatelNative_nativeInit},
            {"setMemoryDiff",                 "(J)V",                                              (void *) Java_com_virjar_ratel_RatelNative_setMemoryDiff},
            {"enableZeldaNative",             "(Ljava/lang/String;Ljava/lang/String;)V",           (void *) Java_com_virjar_ratel_RatelNative_enableZeldaNative},
            {"clearIORedirectConfig",         "()V",                                               (void *) Java_com_virjar_ratel_RatelNative_clearIORedirectConfig},
            {"enableMacAddressFake",          "()V",                                               (void *) Java_com_virjar_ratel_RatelNative_enableMacAddressFake},
            {"setupUserSeed",                 "(I)V",                                              (void *) Java_com_virjar_ratel_RatelNative_setupUserSeed},
            {"traceFilePath",                 "(Ljava/lang/String;)V",                             (void *) Java_com_virjar_ratel_RatelNative_traceFilePath},
            {"enableUnpackComponent",         "(Ljava/lang/String;Z)V",                            (void *) Java_com_virjar_ratel_RatelNative_enableUnpackComponent},
            {"methodDex",                     "(Ljava/lang/reflect/Member;)Ljava/nio/ByteBuffer;", (void *) Java_com_virjar_ratel_RatelNative_methodDex},
            {"hideMaps",                      "()V",                                               (void *) Java_com_virjar_ratel_RatelNative_hideMaps},
            {"mockPathStat",                  "(Ljava/lang/String;J)V",                            (void *) Java_com_virjar_ratel_RatelNative_mockPathStat},
            {"nativeMark",                    "()V",                                               (void *) mark},
            {"fakeMediaDrmId",                "(Ljava/lang/reflect/Method;[B)V",                   (void *) Java_com_virjar_ratel_RatelNative_fakeMediaDrmId},
            {"hookBinder",                    "(Ljava/lang/Class;)V",                              (void *) Java_com_virjar_ratel_RatelNative_hookBinder},
            {"methodNativePtr",               "(Ljava/lang/reflect/Method;)J",                     (void *) Java_com_virjar_ratel_RatelNative_methodNativePtr},
            {"dumpMemory",                    "(JJ)Ljava/nio/ByteBuffer;",                         (void *) Java_com_virjar_ratel_RatelNative_dumpMemory},
    };

    if (env->RegisterNatives(nativeEngineClass, methods,
                             sizeof(methods) / sizeof(JNINativeMethod)) < 0) {
        ALOGE("register native method failed");
        return false;
    }
    measureNativeOffset(env, true);
    return true;
}