//
// Created by alienhe on 2020/12/7.
//

#include <cstring>
#include <VAJni.h>
#include "VmHook.h"
#include "Log.h"
#include "lock.h"
#include <sys/system_properties.h>


namespace FunctionDef {
    typedef jbyteArray (*JNI_mediaDrmGetPropertyArrayFunc)(JNIEnv *env, jobject thiz,
                                                           jstring jname);
}

using namespace FunctionDef;

static struct {
    int native_offset;

    JNI_mediaDrmGetPropertyArrayFunc orig_mediaDrmGetPropertyArrayFunc;
    unsigned char *fakeDrmId;
    int fakeDrmIdLen;
} patchEnv;


unsigned char *jByteArrayToChar(JNIEnv *env, jbyteArray buf) {
    unsigned char *chars = NULL;
    jbyte *bytes;
    bytes = env->GetByteArrayElements(buf, 0);
    int chars_len = env->GetArrayLength(buf);
    chars = new unsigned char[chars_len + 1];
    memset(chars, 0, chars_len + 1);
    memcpy(chars, bytes, chars_len);
    chars[chars_len] = 0;
    env->ReleaseByteArrayElements(buf, bytes, 0);
    return chars;
}

jbyteArray charToJByteArray(JNIEnv *env, unsigned char *buf, int len) {
    jbyteArray array = env->NewByteArray(len);
    env->SetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));
    return array;
}


static jfieldID field_art_method = nullptr;

static int getSdkInt() {
    char sdk[128] = "0";
    __system_property_get("ro.build.version.sdk", sdk);
    return atoi(sdk);
}

static void InitArt(JNIEnv *env) {
    if (getSdkInt() >= 30) {
        if (field_art_method != nullptr) {
            return;
        }
        jclass clazz = env->FindClass("java/lang/reflect/Executable");
        field_art_method = env->GetFieldID(clazz, "artMethod", "J");
    }
}

static void *GetArtMethod(JNIEnv *env, jclass clazz, jmethodID methodId) {
    ALOGI("GetArtMethod in android sdk:%d", getSdkInt());
    if (getSdkInt() >= 30) {
        if (((reinterpret_cast<uintptr_t>(methodId) % 2) != 0)) {
            jobject method = env->ToReflectedMethod(clazz, methodId, true);
            return reinterpret_cast<void *>(env->GetLongField(method, field_art_method));
        }
    }
    return methodId;
}

void mark(JNIEnv *env, jclass clazz) {
    ALOGI("mark method called success!");
}

void measureNativeOffset(JNIEnv *env, bool isArt) {
    InitArt(env);
    jmethodID markMethodId = env->GetStaticMethodID(nativeEngineClass, "nativeMark", "()V");
    // 获取方法的 ArtMethod 指针地址用来计算方法的入口偏移，在 Android 11 以下 jmethodID 就是实际的 ArtMethod 指针，
    // Android 11 以上不返回真实的 ArtMethod 指针，但在 Java Method 对象中有一个 private long artMethod 保存着 ArtMethod 指针
    auto *markArtMethod = static_cast<uintptr_t *>(GetArtMethod(env, nativeEngineClass,
                                                                markMethodId));

    size_t start = (size_t) markArtMethod;
    size_t target = (size_t) mark;

    ALOGI("Success: nativeMark method:%p", markArtMethod);
    ALOGI("Success: mark method:%p", mark);

    int offset = 0;
    bool found = false;
    while (true) {
        if (*((size_t *) (start + offset)) == target) {
            found = true;
            ALOGI("Success: found jni fnPtr offset");
            break;
        }
        offset += 1;
        if (offset >= 100) {
            ALOGE("Error: Cannot find the jni function offset.");
            break;
        }
    }
    if (found) {
        patchEnv.native_offset = offset;
        if (!isArt) {
            patchEnv.native_offset += (sizeof(int) + sizeof(void *));
        }
    }
}


static jbyteArray
new_media_drm_getPropertyByteArray_T(JNIEnv *env, jobject thiz, jstring jname) {
    const char *name = env->GetStringUTFChars(jname, JNI_FALSE);
    jbyteArray result = patchEnv.orig_mediaDrmGetPropertyArrayFunc(env, thiz, jname);
    if (strcmp(name, "deviceUniqueId") != 0) {
        return result;
    }
    if (result != nullptr && env->GetArrayLength(result) > 0) {
        return charToJByteArray(env, patchEnv.fakeDrmId, patchEnv.fakeDrmIdLen);
    } else {
        return result;
    }
}

void replaceJNIFunction(jmethodID method, void *jniFunction) {
    void **funPtr = (void **) (reinterpret_cast<size_t>(method) + patchEnv.native_offset);
    *funPtr = jniFunction;
}

void *getJNIFunction(jmethodID method) {
    void **funPtr = (void **) (reinterpret_cast<size_t>(method) + patchEnv.native_offset);
    return *funPtr;
}


void hookJNIMethod(jmethodID method, void *new_jni_func, void **orig_jni_func) {
    *orig_jni_func = getJNIFunction(method);
    replaceJNIFunction(method, new_jni_func);
}


/**
 * hook {@link android.media.MediaDrm#getPropertyByteArray}
 * 只支持ART
 * http://androidxref.com/8.0.0_r4/xref/frameworks/base/media/jni/android_media_MediaDrm.cpp
 *
 * @param fakeDrmId
 * @param originMethod android.media.MediaDrm#getPropertyByteArray的ArtMethod结构体
 */
void hookMediaDrmGetPropertyByteArrayNative(JNIEnv *env, jobject originJavaMethod,
                                            jbyteArray fakeDrmId) {
    patchEnv.fakeDrmId = jByteArrayToChar(env, fakeDrmId);
    patchEnv.fakeDrmIdLen = env->GetArrayLength(fakeDrmId);

    if (!originJavaMethod) {
        return;
    }
    ALOGD("hookMediaDrmGetPropertyByteArrayNative start");
    jmethodID method = env->FromReflectedMethod(originJavaMethod);
    hookJNIMethod(method, (void *) new_media_drm_getPropertyByteArray_T,
                  (void **) &patchEnv.orig_mediaDrmGetPropertyArrayFunc);
}

jboolean (*BinderHookManager_transactNativeBackup)(JNIEnv *env,
                                                   jobject thiz, jint code,
                                                   jobject data,
                                                   jobject reply,
                                                   jint flags);

jboolean BinderHookManager_transactNativeOrigin(JNIEnv *env,
                                                jclass clazz, jobject thiz, jint code,
                                                jobject data,
                                                jobject reply,
                                                jint flags) {
    return BinderHookManager_transactNativeBackup(env, thiz, code, data, reply, flags);
}

jmethodID hookMethod;
jclass gBinderHookManagerClass;

jboolean BinderHookManager_transactNativeBackup_hook(JNIEnv *env,
                                                     jobject thiz, jint code,
                                                     jobject data,
                                                     jobject reply,
                                                     jint flags) {
    return env->CallStaticBooleanMethod(gBinderHookManagerClass, hookMethod, thiz,
                                        code, data, reply, flags);
}

void hookBinder(JNIEnv *env, jclass binderHookManagerClass) {
    gBinderHookManagerClass = (jclass) (env->NewGlobalRef(binderHookManagerClass));
    /**
     *         //android.os.BinderProxy#transactNative
        Class<?> binderProxyClass = RposedHelpers.findClass("android.os.BinderProxy", ClassLoader.getSystemClassLoader());
        Method targetMethod = RposedHelpers.findMethodExact(binderProxyClass, "transactNative", int.class, Parcel.class, Parcel.class, int.class);

        backupMethod = RposedHelpers.findMethodExact(BinderHookManager.class, "transactNativeBackup", int.class, Parcel.class, Parcel.class, int.class);
        Method hookMethod = RposedHelpers.findMethodExact(BinderHookManager.class, "transactNativeHookMethod", IBinder.class, int.class, Parcel.class, Parcel.class, int.class);


     */
    SandHook::StopTheWorld stopTheWorld;

    jclass binderProxyClass = env->FindClass("android/os/BinderProxy");
    //android.os.Parcel
    jmethodID targetMethod = env->GetMethodID(binderProxyClass, "transactNative",
                                              "(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z");

    //
    hookMethod = env->GetStaticMethodID(binderHookManagerClass, "transactNativeHookMethod",
                                        "(Landroid/os/IBinder;ILandroid/os/Parcel;Landroid/os/Parcel;I)Z");


    hookJNIMethod(targetMethod, (void *) BinderHookManager_transactNativeBackup_hook,
                  (void **) &BinderHookManager_transactNativeBackup);


    static JNINativeMethod methods[] = {
            {"transactNativeOrigin",
                    "(Landroid/os/IBinder;ILandroid/os/Parcel;Landroid/os/Parcel;I)Z",
                    (void *) BinderHookManager_transactNativeOrigin
            }
    };
    env->RegisterNatives(binderHookManagerClass, methods, 1);
}


