#include "cast.h"
#include "trampoline.h"
#include "native_api.h"
#include "elf_util.h"
#include "VAJni.h"
#include <jni.h>
#include <Foundation/Log.h>
#include <lock.h>

SandHook::TrampolineManager &trampolineManager = SandHook::TrampolineManager::get();

int SDK_INT = 0;
bool HOST_DEBUG = false;
bool RATEL_BUILD_DEBUG = false;

enum HookMode {
    AUTO = 0,
    INLINE = 1,
    REPLACE = 2
};

HookMode gHookMode = AUTO;

struct BatchHookItem {
    art::mirror::ArtMethod *originMethod;
    art::mirror::ArtMethod *hookMethod;
    art::mirror::ArtMethod *backupMethod;
    HookMode hookMode;
    BatchHookItem *next;
};

BatchHookItem *gBatchHookItemFirst = nullptr;
bool now_batch_hook_status = false;

void ensureMethodCached(art::mirror::ArtMethod *hookMethod, art::mirror::ArtMethod *backupMethod) {
    if (SDK_INT >= ANDROID_P)
        return;

    SandHook::StopTheWorld stopTheWorld;

    uint32_t index = backupMethod->getDexMethodIndex();
    if (SDK_INT < ANDROID_O2) {
        hookMethod->setDexCacheResolveItem(index, backupMethod);
    } else {
        Size cacheSize = 1024;
        Size slotIndex = index % cacheSize;
        Size newCachedMethodsArray = reinterpret_cast<Size>(calloc(cacheSize, BYTE_POINT * 2));
        unsigned int one = 1;
        memcpy(reinterpret_cast<void *>(newCachedMethodsArray + BYTE_POINT), &one, 4);
        memcpy(reinterpret_cast<void *>(newCachedMethodsArray + BYTE_POINT * 2 * slotIndex),
               (&backupMethod),
               BYTE_POINT
        );
        memcpy(reinterpret_cast<void *>(newCachedMethodsArray + BYTE_POINT * 2 * slotIndex +
                                        BYTE_POINT),
               &index,
               4
        );
        hookMethod->setDexCacheResolveList(&newCachedMethodsArray);
    }
}

extern "C"
void JNICALL
Java_com_virjar_ratel_sandhook_SandHook_ensureDeclareClass(JNIEnv *env, jclass type,
                                                           jobject originMethod,
                                                           jobject backupMethod) {
    if (originMethod == nullptr || backupMethod == nullptr)
        return;
    art::mirror::ArtMethod *origin = getArtMethod(env->FromReflectedMethod(originMethod));
    art::mirror::ArtMethod *backup = getArtMethod(env->FromReflectedMethod(backupMethod));
    if (origin->getDeclaringClass() != backup->getDeclaringClass()) {
        ALOGW("declaring class has been moved!");
        backup->setDeclaringClass(origin->getDeclaringClass());
    }
}
//void ensureDeclareClass(JNIEnv *env, jclass type, jobject originMethod,
//                        jobject backupMethod) {
//
//}

bool doHookWithReplacement(JNIEnv *env,
                           art::mirror::ArtMethod *originMethod,
                           art::mirror::ArtMethod *hookMethod,
                           art::mirror::ArtMethod *backupMethod) {

    if (!hookMethod->compile(env)) {
        hookMethod->disableCompilable();
    }


    if (SDK_INT > ANDROID_N && SDK_INT < ANDROID_Q) {
        forceProcessProfiles();
    }
    if ((SDK_INT >= ANDROID_N && SDK_INT <= ANDROID_P)
        || (SDK_INT >= ANDROID_Q && !originMethod->isAbstract())) {
        originMethod->setHotnessCount(0);
    }

    if (backupMethod != nullptr) {
        originMethod->backup(backupMethod);
        backupMethod->disableCompilable();
        if (!backupMethod->isStatic()) {
            backupMethod->setPrivate();
        }
        backupMethod->flushCache();
    }

    originMethod->disableCompilable();
    hookMethod->disableCompilable();
    hookMethod->flushCache();

    originMethod->disableInterpreterForO();
    originMethod->disableFastInterpreterForQ();

    SandHook::HookTrampoline *hookTrampoline = trampolineManager.installReplacementTrampoline(
            originMethod, hookMethod, backupMethod);
    if (hookTrampoline != nullptr) {
        originMethod->setQuickCodeEntry(hookTrampoline->replacement->getCode());
        void *entryPointFromInterpreter = hookMethod->getInterpreterCodeEntry();
        if (entryPointFromInterpreter != nullptr) {
            originMethod->setInterpreterCodeEntry(entryPointFromInterpreter);
        }
        if (hookTrampoline->callOrigin != nullptr) {
            backupMethod->setQuickCodeEntry(hookTrampoline->callOrigin->getCode());
            backupMethod->flushCache();
        }
        originMethod->flushCache();
        return true;
    } else {
        return false;
    }
}

bool doHookWithInline(JNIEnv *env,
                      art::mirror::ArtMethod *originMethod,
                      art::mirror::ArtMethod *hookMethod,
                      art::mirror::ArtMethod *backupMethod) {

    //fix >= 8.1
    if (!hookMethod->compile(env)) {
        hookMethod->disableCompilable();
    }

    originMethod->disableCompilable();
    if (SDK_INT > ANDROID_N && SDK_INT < ANDROID_Q) {
        forceProcessProfiles();
    }
    if ((SDK_INT >= ANDROID_N && SDK_INT <= ANDROID_P)
        || (SDK_INT >= ANDROID_Q && !originMethod->isAbstract())) {
        originMethod->setHotnessCount(0);
    }
    originMethod->flushCache();

    SandHook::HookTrampoline *hookTrampoline = trampolineManager.installInlineTrampoline(
            originMethod, hookMethod, backupMethod);

    if (hookTrampoline == nullptr)
        return false;

    hookMethod->flushCache();
    if (hookTrampoline->callOrigin != nullptr) {
        //backup
        originMethod->backup(backupMethod);
        backupMethod->setQuickCodeEntry(hookTrampoline->callOrigin->getCode());
        backupMethod->disableCompilable();
        if (!backupMethod->isStatic()) {
            backupMethod->setPrivate();
        }
        backupMethod->flushCache();
    }
    return true;
}


extern "C"
jboolean JNICALL
Java_com_virjar_ratel_sandhook_SandHook_initNative(JNIEnv *env, jclass type, jint sdk,
                                                   jboolean hostDebug,
                                                   jboolean ratelBuildDebug) {

    SDK_INT = sdk;
    HOST_DEBUG = hostDebug;
    RATEL_BUILD_DEBUG = ratelBuildDebug;
    SandHook::CastCompilerOptions::init(env);
    initHideApi(env);
    SandHook::CastArtMethod::init(env);
    trampolineManager.init(SandHook::CastArtMethod::entryPointQuickCompiled->getOffset());
    return JNI_TRUE;

}

extern "C"
jint JNICALL
Java_com_virjar_ratel_sandhook_SandHook_hookMethod(JNIEnv *env, jclass type, jobject originMethod,
                                                   jobject hookMethod, jobject backupMethod,
                                                   jint hookMode) {
    art::mirror::ArtMethod *origin = getArtMethod(env->FromReflectedMethod(originMethod));
    art::mirror::ArtMethod *hook = getArtMethod(env->FromReflectedMethod(hookMethod));
    art::mirror::ArtMethod *backup =
            backupMethod == NULL ? nullptr : getArtMethod(env->FromReflectedMethod(backupMethod));


    bool isInlineHook = false;

    int mode = reinterpret_cast<int>(hookMode);


    if (mode == INLINE) {
        if (!origin->isCompiled()) {
            if (SDK_INT >= ANDROID_N) {
                isInlineHook = origin->compile(env);
            }
        } else {
            isInlineHook = true;
        }
        goto label_hook;
    } else if (mode == REPLACE) {
        isInlineHook = false;
        goto label_hook;
    }

    if (origin->isAbstract()) {
        isInlineHook = false;
    } else if (gHookMode != AUTO) {
        if (gHookMode == INLINE) {
            isInlineHook = origin->compile(env);
        } else {
            isInlineHook = false;
        }
    } else if (SDK_INT >= ANDROID_O) {
        isInlineHook = false;
    } else if (!origin->isCompiled()) {
        if (SDK_INT >= ANDROID_N) {
            isInlineHook = origin->compile(env);
        } else {
            isInlineHook = false;
        }
    } else {
        isInlineHook = true;
    }

    label_hook:
    //suspend other threads
    // SandHook::AutoLock waitJitEnd(jitMutex);
    if (now_batch_hook_status) {
        // ratel框架hook功能，不需要马上得到hook是否成功的结果，所以执行批量hook功能
        // 这可以避免频繁的StopTheWorld
        BatchHookItem *batchHookItem = static_cast<BatchHookItem *>(malloc(
                sizeof(BatchHookItem)));
        batchHookItem->next = gBatchHookItemFirst;
        batchHookItem->originMethod = origin;
        batchHookItem->hookMethod = hook;
        batchHookItem->backupMethod = backup;
        batchHookItem->hookMode = (isInlineHook && trampolineManager.canSafeInline(origin)) ? INLINE
                                                                                            : REPLACE;
        gBatchHookItemFirst = batchHookItem;
        return batchHookItem->hookMode;
    } else {
        SandHook::StopTheWorld stopTheWorld;
        if (isInlineHook && trampolineManager.canSafeInline(origin)) {
            return doHookWithInline(env, origin, hook, backup) ? INLINE : -1;
        } else {
            return doHookWithReplacement(env, origin, hook, backup) ? REPLACE : -1;
        }
    }
}


//    public static native void setBatchHook(boolean batchHookStatus);
extern "C"
JNIEXPORT void JNICALL
Java_com_virjar_ratel_sandhook_SandHook_setBatchHook(JNIEnv *env, jclass type,
                                                     jboolean batchHookStatus) {
    now_batch_hook_status = batchHookStatus;
    if (batchHookStatus) {
        return;
    }
    SandHook::StopTheWorld stopTheWorld;
    while (gBatchHookItemFirst != nullptr) {
        if (gBatchHookItemFirst->hookMode == INLINE) {
            if (!doHookWithInline(env, gBatchHookItemFirst->originMethod,
                                  gBatchHookItemFirst->hookMethod,
                                  gBatchHookItemFirst->backupMethod)) {
                ALOGE("hook failed in batch hook in doHookWithInline mode");
            }
        } else if (gBatchHookItemFirst->hookMode == REPLACE) {
            if (!doHookWithReplacement(env, gBatchHookItemFirst->originMethod,
                                       gBatchHookItemFirst->hookMethod,
                                       gBatchHookItemFirst->backupMethod)) {
                ALOGE("hook failed in batch hook in doHookWithReplacement mode");
            }
        } else {
            ALOGE("error hook mode in batch hook:%d", gBatchHookItemFirst->hookMode);
        }
        BatchHookItem *now_item = gBatchHookItemFirst;
        gBatchHookItemFirst = gBatchHookItemFirst->next;
        free(now_item);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_virjar_ratel_sandhook_SandHook_makeInitializedClassVisibilyInitialized(JNIEnv *env, jclass clazz,
                                                                                jlong self) {
    MakeInitializedClassVisibilyInitialized(reinterpret_cast<void*>(self));
}

extern "C"
void JNICALL
Java_com_virjar_ratel_sandhook_SandHook_ensureMethodCached(JNIEnv *env, jclass type, jobject hook,
                                                           jobject backup) {
    art::mirror::ArtMethod *hookeMethod = getArtMethod(env->FromReflectedMethod(hook));
    art::mirror::ArtMethod *backupMethod =
            backup == NULL ? nullptr : getArtMethod(env->FromReflectedMethod(backup));
    ensureMethodCached(hookeMethod, backupMethod);
}

extern "C"
jboolean JNICALL
Java_com_virjar_ratel_sandhook_SandHook_compileMethod(JNIEnv *env, jclass type, jobject member) {

    if (member == nullptr)
        return JNI_FALSE;
    art::mirror::ArtMethod *method = getArtMethod(env->FromReflectedMethod(member));

    if (method == nullptr)
        return JNI_FALSE;

    if (!method->isCompiled()) {
        SandHook::StopTheWorld stopTheWorld;
        if (!method->compile(env)) {
            if (SDK_INT >= ANDROID_N) {
                method->disableCompilable();
                method->flushCache();
            }
            return JNI_FALSE;
        } else {
            return JNI_TRUE;
        }
    } else {
        return JNI_TRUE;
    }

}

extern "C"
jboolean JNICALL
Java_com_virjar_ratel_sandhook_SandHook_deCompileMethod(JNIEnv *env, jclass type, jobject member,
                                                        jboolean disableJit) {

    if (member == nullptr)
        return JNI_FALSE;
    art::mirror::ArtMethod *method = getArtMethod(env->FromReflectedMethod(member));


    if (method == nullptr)
        return JNI_FALSE;

    if (disableJit) {
        method->disableCompilable();
    }

    if (method->isCompiled()) {
        SandHook::StopTheWorld stopTheWorld;
        if (SDK_INT >= ANDROID_N) {
            method->disableCompilable();
        }
        return static_cast<jboolean>(method->deCompile());
    } else {
        return JNI_TRUE;
    }

}

extern "C"
jobject JNICALL
Java_com_virjar_ratel_sandhook_SandHook_getObjectNative(JNIEnv *env, jclass type, jlong thread,
                                                        jlong address) {
    return getJavaObject(env, thread ? reinterpret_cast<void *>(thread) : getCurrentThread(),
                         reinterpret_cast<void *>(address));
}

extern "C"
jboolean JNICALL
Java_com_virjar_ratel_sandhook_SandHook_canGetObject(JNIEnv *env, jclass type) {
    return static_cast<jboolean>(canGetObject());
}

extern "C"
void JNICALL
Java_com_virjar_ratel_sandhook_SandHook_setHookMode(JNIEnv *env, jclass type, jint mode) {
    gHookMode = static_cast<HookMode>(mode);
}

extern "C"
void JNICALL
Java_com_virjar_ratel_sandhook_SandHook_setInlineSafeCheck(JNIEnv *env, jclass type,
                                                           jboolean check) {
    trampolineManager.inlineSecurityCheck = check;
}

extern "C"
void JNICALL
Java_com_virjar_ratel_sandhook_SandHook_skipAllSafeCheck(JNIEnv *env, jclass type, jboolean skip) {
    trampolineManager.skipAllCheck = skip;
}
extern "C"
jboolean JNICALL
Java_com_virjar_ratel_sandhook_SandHook_is64Bit(JNIEnv *env, jclass type) {
    return static_cast<jboolean>(BYTE_POINT == 8);
}

extern "C"
jboolean JNICALL
Java_com_virjar_ratel_sandhook_SandHook_disableVMInline(JNIEnv *env, jclass type) {
    if (SDK_INT < ANDROID_N)
        return JNI_FALSE;
    replaceUpdateCompilerOptionsQ();
    art::CompilerOptions *compilerOptions = getGlobalCompilerOptions();
    if (compilerOptions == nullptr)
        return JNI_FALSE;
    return static_cast<jboolean>(disableJitInline(compilerOptions));
}

extern "C"
jboolean JNICALL
Java_com_virjar_ratel_sandhook_SandHook_disableDex2oatInline(JNIEnv *env, jclass type,
                                                             jboolean disableDex2oat) {
    return static_cast<jboolean>(hookDex2oat(disableDex2oat));
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_virjar_ratel_sandhook_SandHook_setNativeEntry(JNIEnv *env, jclass type, jobject origin,
                                                       jobject hook, jlong jniTrampoline) {
    if (origin == nullptr || hook == NULL)
        return JNI_FALSE;
    art::mirror::ArtMethod *hookMethod = getArtMethod(env->FromReflectedMethod(hook));
    art::mirror::ArtMethod *originMethod = getArtMethod(env->FromReflectedMethod(origin));
    originMethod->backup(hookMethod);
    hookMethod->setNative();
    hookMethod->setQuickCodeEntry(SandHook::CastArtMethod::genericJniStub);
    hookMethod->setJniCodeEntry(reinterpret_cast<void *>(jniTrampoline));
    hookMethod->disableCompilable();
    hookMethod->flushCache();
    return JNI_TRUE;
}


static jclass class_pending_hook = nullptr;
static jmethodID method_class_init = nullptr;

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_virjar_ratel_sandhook_SandHook_initForPendingHook(JNIEnv *env, jclass type) {
    class_pending_hook = static_cast<jclass>(env->NewGlobalRef(
            env->FindClass(sandHookPendingHookHandlerClass)));
    method_class_init = env->GetStaticMethodID(class_pending_hook, "onClassInit", "(J)V");
    auto class_init_handler = [](void *clazz_ptr) {
        ensureEnvCreated()->CallStaticVoidMethod(class_pending_hook, method_class_init,
                                                 (jlong) clazz_ptr);
        ensureEnvCreated()->ExceptionClear();
    };
    return static_cast<jboolean>(hookClassInit(class_init_handler));
}


extern "C"
void JNICALL
Java_com_virjar_ratel_sandhook_ClassNeverCall_neverCallNative(JNIEnv *env, jobject instance) {
    int a = 1 + 1;
    int b = a + 1;
}

extern "C"
void JNICALL
Java_com_virjar_ratel_sandhook_ClassNeverCall_neverCallNative2(JNIEnv *env, jobject instance) {
    int a = 4 + 3;
    int b = 9 + 6;
}


extern "C"
void JNICALL
Java_com_virjar_ratel_sandhook_test_TestClass_jni_1test(JNIEnv *env, jobject instance) {
    int a = 1 + 1;
    int b = a + 1;
}



static JNINativeMethod jniSandHook[] = {
        {
                "initNative",
                "(IZZ)Z",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_initNative
        },
        {
                "hookMethod",
                "(Ljava/lang/reflect/Member;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;I)I",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_hookMethod
        },
        {
                "ensureMethodCached",
                "(Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)V",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_ensureMethodCached
        },
        {
                "ensureDeclareClass",
                "(Ljava/lang/reflect/Member;Ljava/lang/reflect/Method;)V",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_ensureDeclareClass
        },
        {
                "compileMethod",
                "(Ljava/lang/reflect/Member;)Z",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_compileMethod
        },
        {
                "deCompileMethod",
                "(Ljava/lang/reflect/Member;Z)Z",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_deCompileMethod
        },
        {
                "getObjectNative",
                "(JJ)Ljava/lang/Object;",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_getObjectNative
        },
        {
                "canGetObject",
                "()Z",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_canGetObject
        },
        {
                "setHookMode",
                "(I)V",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_setHookMode
        },
        {
                "setInlineSafeCheck",
                "(Z)V",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_setInlineSafeCheck
        },
        {
                "skipAllSafeCheck",
                "(Z)V",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_skipAllSafeCheck
        },
        {
                "is64Bit",
                "()Z",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_is64Bit
        },
        {
                "disableVMInline",
                "()Z",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_disableVMInline
        },
        {
                "disableDex2oatInline",
                "(Z)Z",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_disableDex2oatInline
        },
        {
                "setNativeEntry",
                "(Ljava/lang/reflect/Member;Ljava/lang/reflect/Member;J)Z",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_setNativeEntry
        },
        {
                "initForPendingHook",
                "()Z",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_initForPendingHook
        },
        {
                "setBatchHook",
                "(Z)V",
                (void *) Java_com_virjar_ratel_sandhook_SandHook_setBatchHook
        },
        {
                "makeInitializedClassVisibilyInitialized",
                "(J)V",
                (void*) Java_com_virjar_ratel_sandhook_SandHook_makeInitializedClassVisibilyInitialized
        }
};

static JNINativeMethod jniNeverCall[] = {
        {
                "neverCallNative",
                "()V",
                (void *) Java_com_virjar_ratel_sandhook_ClassNeverCall_neverCallNative
        },
        {
                "neverCallNative2",
                "()V",
                (void *) Java_com_virjar_ratel_sandhook_ClassNeverCall_neverCallNative2
        }
};

bool
registerSandHookClassNevelCallMethod(JNIEnv *env, const char *sandHookClassNeverCallClassChars) {
    jclass classNeverCallClass = env->FindClass(sandHookClassNeverCallClassChars);
    if (classNeverCallClass == nullptr) {
        return false;
    }
    return env->RegisterNatives(classNeverCallClass, jniNeverCall,
                                sizeof(jniNeverCall) / sizeof(JNINativeMethod)) >= 0;
}

bool registerSandHookNativeMethod(JNIEnv *env, const char *sandHookClassChars) {
    jclass sandHookClass = env->FindClass(sandHookClassChars);
    if (sandHookClass == nullptr) {
        return false;
    }
    return env->RegisterNatives(sandHookClass, jniSandHook,
                                sizeof(jniSandHook) / sizeof(JNINativeMethod)) >= 0;
}

