
// VirtualApp Native Project
//

#include <MapsHideHandler.h>
#include "VAJni.h"
#include "Log.h"

#define JAVA_MAP_CLASS_NAME "java/util/Map"

char *artMethodSizeTestCLass;
char *sandHookMethodResolverClass;
char *sandClassNeverCallClass;
char *sandSandHookClass;
char *sandHookConfigClass;
char *sandHookPendingHookHandlerClass;
char *propertiesMockItemClass;
char *selfExplosionClass;

jstring queryClassImplName(JNIEnv *env, jobject bindData, const char *key) {
    jclass mapClass = env->FindClass(JAVA_MAP_CLASS_NAME);
    //V get(Object key);
    jmethodID getMethodId = env->GetMethodID(mapClass, "get",
                                             "(Ljava/lang/Object;)Ljava/lang/Object;");

    return (jstring) (env->CallObjectMethod(bindData, getMethodId, env->NewStringUTF(key)));
}


extern "C"
jstring JNICALL
Java_com_virjar_ratel_NativeBridge_bridgeInitNative(JNIEnv *env, jclass jclazz, jobject bindData,
                                                    jobject context, jstring originPkgName) {

    jstring ratelNativeClass = queryClassImplName(env, bindData, "RATEL_NATIVE");
    jstring sandHookClass = queryClassImplName(env, bindData, "SAND_HOOK");
    jstring sandHookClassNeverCallClass = queryClassImplName(env, bindData,
                                                             "SAND_CLASS_NEVER_CALL");
    propertiesMockItemClass = strdup(
            ScopeUtfString(
                    queryClassImplName(env, bindData, "RATEL_PROPERTIES_MOCK_ITEM")).c_str());

    if (ratelNativeClass == nullptr) {
        return env->NewStringUTF("can not find class define for :RATEL_NATIVE");
    }
    if (!registerIOMethod(env, ScopeUtfString(ratelNativeClass).c_str())) {
        return env->NewStringUTF("register io method failed");
    }


    if (sandHookClass == nullptr) {
        return env->NewStringUTF("can not find class define for :SAND_HOOK");
    }
    if (!registerSandHookNativeMethod(env, ScopeUtfString(sandHookClass).c_str())) {
        return env->NewStringUTF("register sandhook method failed");
    }

    if (sandHookClassNeverCallClass == nullptr) {
        return env->NewStringUTF("can not find class define for :SAND_CLASS_NEVER_CALL");
    }
    if (!registerSandHookClassNevelCallMethod(env, ScopeUtfString(
            sandHookClassNeverCallClass).c_str())) {
        return env->NewStringUTF("register ClassNeverCall method failed");
    }


    artMethodSizeTestCLass = strdup(
            ScopeUtfString(queryClassImplName(env, bindData, "SAND_METHOD_SIZE_TEST")).c_str());

    sandHookMethodResolverClass = strdup(
            ScopeUtfString(queryClassImplName(env, bindData, "SAND_METHOD_RESOLVER")).c_str());

    sandHookConfigClass = strdup(
            ScopeUtfString(queryClassImplName(env, bindData, "SAND_HOOK_CONFIG")).c_str());

    sandHookPendingHookHandlerClass = strdup(
            ScopeUtfString(queryClassImplName(env, bindData, "RAND_HOOK_PENDING_HOOK")).c_str());

    selfExplosionClass = strdup(
            ScopeUtfString(queryClassImplName(env, bindData, "RATEL_A")).c_str());


    sandClassNeverCallClass = strdup(ScopeUtfString(sandHookClassNeverCallClass).c_str());

    sandSandHookClass = strdup(ScopeUtfString(sandHookClass).c_str());

    return nullptr;
}


JavaVM *vm;

jclass nativeBridgeClass;


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *_vm, void *reserved) {
    //first time to hidden self
    doMapsHide(true);
    vm = _vm;
    JNIEnv *env;
    _vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);

    nativeBridgeClass = (jclass) (env->NewGlobalRef(
            env->FindClass(RATEL_NATIVE_BRIDGE_CLASS_NAME)));
    static JNINativeMethod bridgeIntMethod[] = {
            //java.util.Map android.content.Context
            {"bridgeInitNative", "(Ljava/util/Map;Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;", (void *) Java_com_virjar_ratel_NativeBridge_bridgeInitNative}
    };
    if (env->RegisterNatives(nativeBridgeClass, bridgeIntMethod, 1) < 0) {
        ALOGE("register bridgeInitNative method failed");
        return JNI_ERR;
    }

    //return sandhook_JNI_OnLoad(vm, reserved);
    return JNI_VERSION_1_6;
}


JNIEnv *getEnv() {
    JNIEnv *env;
    vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
    return env;
}

JNIEnv *ensureEnvCreated() {
    JNIEnv *env = getEnv();
    if (env == nullptr) {
        vm->AttachCurrentThread(&env, nullptr);
    }
    return env;
}

void DetachCurrentThread() {
    vm->DetachCurrentThread();
}

bool IS_EXECVE_SUB_PROCESS = false;
//exec 的时候，附加这个函数，将本so附加到子进程，实现子进程的iohook
extern "C" __attribute__((constructor)) void _init(void) {
    IS_EXECVE_SUB_PROCESS = true;
    IOUniformer::init_env_before_all();
}