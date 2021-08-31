//
// Created by SwiftGan on 2019/4/12.
//

#include <syscall.h>
#include <cstdlib>
#include <cstring>
#include <unistd.h>
#include <Foundation/Log.h>
#include <Substrate/CydiaSubstrate.h>
#include <elf_util.h>
#include <Jni/VAJni.h>
#include <Foundation/dlfcn_nougat.h>
#include "native_api.h"
#include "arch/arch.h"
#include "Substrate/SubstrateHook.h"
#include "ExecveHandler.h"
#include "trampoline.h"
//#include "ratel_build_type.h"
//#include "lock.h"
#include <elf.h>


extern int SDK_INT;

extern "C" {
int inline getArrayItemCount(char *const array[]) {
    int i;
    for (i = 0; array[i]; ++i);
    return i;
}

bool isSandHooker(char *const args[]) {
    int orig_arg_count = getArrayItemCount(args);

    for (int i = 0; i < orig_arg_count; i++) {
        if (strstr(args[i], "SandHooker")) {
            ALOGE("skip dex2oat hooker!");
            return true;
        }
    }

    return false;
}

char **build_new_argv(char *const argv[]) {

    int orig_argv_count = getArrayItemCount(argv);

    int new_argv_count = orig_argv_count + 2;
    char **new_argv = (char **) malloc(new_argv_count * sizeof(char *));
    int cur = 0;
    for (int i = 0; i < orig_argv_count; ++i) {
        new_argv[cur++] = argv[i];
    }

    if (SDK_INT >= ANDROID_L2 && SDK_INT < ANDROID_Q) {
        new_argv[cur++] = (char *) "--compile-pic";
    }
    if (SDK_INT >= ANDROID_M) {
        new_argv[cur++] = (char *) (SDK_INT > ANDROID_N2 ? "--inline-max-code-units=0"
                                                         : "--inline-depth-limit=0");
    }

    new_argv[cur] = nullptr;

    return new_argv;
}


void *jitCompilerHandle = nullptr;
bool (*jitCompileMethod)(void *, void *, void *, bool) = nullptr;
bool (*jitCompileMethodQ)(void *, void *, void *, bool, bool) = nullptr;

//std::mutex jitMutex;
//
////编译器内部的编译方法，jit_compile_method函数太短，是一个跳转函数，跳转到JitInnerCompileMethod
//bool (*JitInnerCompileMethodBackup)(void *, void *, void *, bool) = nullptr;
//bool new_JitInnerCompileMethod(void *thiz, void *thread, void *artMethod, bool osr) {
//    //SandHook::AutoLock autoLock(jitMutex);
//    return JitInnerCompileMethodBackup(thiz, thread, artMethod, osr);
//}


void (*innerSuspendVM)() = nullptr;
void (*innerResumeVM)() = nullptr;

jobject (*addWeakGlobalRef)(JavaVM *, void *, void *) = nullptr;

art::jit::JitCompiler **globalJitCompileHandlerAddr = nullptr;

//for Android Q
void (**origin_jit_update_options)(void *) = nullptr;

void (*profileSaver_ForceProcessProfiles)() = nullptr;

//for Android R
void *jniIdManager = nullptr;
ArtMethod *(*origin_DecodeArtMethodId)(void *thiz, jmethodID jmethodId) = nullptr;
ArtMethod *replace_DecodeArtMethodId(void *thiz, jmethodID jmethodId) {
    jniIdManager = thiz;
    return origin_DecodeArtMethodId(thiz, jmethodId);
}

bool
(*origin_ShouldUseInterpreterEntrypoint)(ArtMethod *artMethod, const void *quick_code) = nullptr;
bool replace_ShouldUseInterpreterEntrypoint(ArtMethod *artMethod, const void *quick_code) {
    if (SandHook::TrampolineManager::get().methodHooked(artMethod) && quick_code != nullptr) {
        return false;
    }
    return origin_ShouldUseInterpreterEntrypoint(artMethod, quick_code);
}

// paths

const char *art_lib_path;
const char *jit_lib_path;

JavaVM *jvm;

void (*class_init_callback)(void *) = nullptr;

void (*backup_fixup_static_trampolines)(void *, void *) = nullptr;

void *(*backup_mark_class_initialized)(void *, void *, uint32_t *) = nullptr;

void (*backup_update_methods_code)(void *, ArtMethod *, const void *) = nullptr;

void *(*make_initialized_classes_visibly_initialized_)(void *, void *, bool) = nullptr;

void *runtime_instance_ = nullptr;


void initHideApi(JNIEnv *env) {

    env->GetJavaVM(&jvm);

    if (BYTE_POINT == 8) {
        if (SDK_INT >= ANDROID_Q) {
            art_lib_path = "/lib64/libart.so";
            jit_lib_path = "/lib64/libart-compiler.so";
        } else {
            art_lib_path = "/system/lib64/libart.so";
            jit_lib_path = "/system/lib64/libart-compiler.so";
        }
    } else {
        if (SDK_INT >= ANDROID_Q) {
            art_lib_path = "/lib/libart.so";
            jit_lib_path = "/lib/libart-compiler.so";
        } else {
            art_lib_path = "/system/lib/libart.so";
            jit_lib_path = "/system/lib/libart-compiler.so";
        }
    }

    //init compile
    if (SDK_INT >= ANDROID_N) {
        globalJitCompileHandlerAddr = reinterpret_cast<art::jit::JitCompiler **>(getSymCompat(
                art_lib_path, "_ZN3art3jit3Jit20jit_compiler_handle_E"));
        if (SDK_INT >= ANDROID_Q) {
            jitCompileMethodQ = reinterpret_cast<bool (*)(void *, void *, void *, bool,
                                                          bool)>(getSymCompat(jit_lib_path,
                                                                              "jit_compile_method"));
        } else {
            jitCompileMethod = reinterpret_cast<bool (*)(void *, void *, void *,
                                                         bool)>(getSymCompat(jit_lib_path,
                                                                             "jit_compile_method"));
//            // 勾住JIT编译器，增加锁机制，避免我们的编译动作和JIT编译器动作存在锁冲突
//            //_ZN3art3jit11JitCompiler13CompileMethodEPNS_6ThreadEPNS_9ArtMethodEb
//            void *art_jitCompiler_compileMethod = getSymCompat(jit_lib_path,
//                                                               "_ZN3art3jit11JitCompiler13CompileMethodEPNS_6ThreadEPNS_9ArtMethodEb");
//            if (art_jitCompiler_compileMethod == nullptr) {
//                ALOGW("can not find symbol _ZN3art3jit11JitCompiler13CompileMethodEPNS_6ThreadEPNS_9ArtMethodEb");
//            } else {
//                MSHookFunction(art_jitCompiler_compileMethod, (void *) new_JitInnerCompileMethod,
//                               reinterpret_cast<void **>(&JitInnerCompileMethodBackup));
//            }

        }

        auto jit_load = getSymCompat(jit_lib_path, "jit_load");
        if (jit_load) {
            if (SDK_INT >= ANDROID_Q) {
                // Android 10：void* jit_load()
                // Android 11: JitCompilerInterface* jit_load()
                jitCompilerHandle = reinterpret_cast<void *(*)()>(jit_load)();
            } else {
                // void* jit_load(bool* generate_debug_info)
                bool generate_debug_info = false;
                jitCompilerHandle = reinterpret_cast<void *(*)(void *)>(jit_load)(
                        &generate_debug_info);
            }
            jitCompilerHandle = getGlobalJitCompiler();
        }


        if (jitCompilerHandle != nullptr) {
            art::CompilerOptions *compilerOptions = getCompilerOptions(
                    reinterpret_cast<art::jit::JitCompiler *>(jitCompilerHandle));
            disableJitInline(compilerOptions);
        }

    }


    //init suspend
    innerSuspendVM = reinterpret_cast<void (*)()>(getSymCompat(art_lib_path,
                                                               "_ZN3art3Dbg9SuspendVMEv"));
    innerResumeVM = reinterpret_cast<void (*)()>(getSymCompat(art_lib_path,
                                                              "_ZN3art3Dbg8ResumeVMEv"));


    //init for getObject & JitCompiler
    const char *add_weak_ref_sym;
    if (SDK_INT < ANDROID_M) {
        add_weak_ref_sym = "_ZN3art9JavaVMExt22AddWeakGlobalReferenceEPNS_6ThreadEPNS_6mirror6ObjectE";
    } else if (SDK_INT < ANDROID_N) {
        add_weak_ref_sym = "_ZN3art9JavaVMExt16AddWeakGlobalRefEPNS_6ThreadEPNS_6mirror6ObjectE";
    } else if (SDK_INT <= ANDROID_N2) {
        add_weak_ref_sym = "_ZN3art9JavaVMExt16AddWeakGlobalRefEPNS_6ThreadEPNS_6mirror6ObjectE";
    } else {
        add_weak_ref_sym = "_ZN3art9JavaVMExt16AddWeakGlobalRefEPNS_6ThreadENS_6ObjPtrINS_6mirror6ObjectEEE";
    }

    addWeakGlobalRef = reinterpret_cast<jobject (*)(JavaVM *, void *,
                                                    void *)>(getSymCompat(art_lib_path,
                                                                          add_weak_ref_sym));

    if (SDK_INT >= ANDROID_Q) {
        origin_jit_update_options = reinterpret_cast<void (**)(void *)>(getSymCompat(art_lib_path,
                                                                                     "_ZN3art3jit3Jit20jit_update_options_E"));
    }

    if (SDK_INT > ANDROID_N) {
        profileSaver_ForceProcessProfiles = reinterpret_cast<void (*)()>(getSymCompat(art_lib_path,
                                                                                      "_ZN3art12ProfileSaver20ForceProcessProfilesEv"));
    }

    if (SDK_INT >= ANDROID_R) {
        const char *symbol_decode_method = sizeof(void *) == 8
                                           ? "_ZN3art3jni12JniIdManager15DecodeGenericIdINS_9ArtMethodEEEPT_m"
                                           : "_ZN3art3jni12JniIdManager15DecodeGenericIdINS_9ArtMethodEEEPT_j";
        void *decodeArtMethod = getSymCompat(art_lib_path, symbol_decode_method);
        if (art_lib_path != nullptr) {
//            origin_DecodeArtMethodId = reinterpret_cast<ArtMethod *(*)(void *,
//                                                                       jmethodID)>(hook_native(
//                    decodeArtMethod,
//                    reinterpret_cast<void *>(replace_DecodeArtMethodId)));

            MSHookFunction(decodeArtMethod, (void *) replace_DecodeArtMethodId,
                           (void **) &origin_DecodeArtMethodId);

        }
        void *shouldUseInterpreterEntrypoint = getSymCompat(art_lib_path,
                                                            "_ZN3art11ClassLinker30ShouldUseInterpreterEntrypointEPNS_9ArtMethodEPKv");
        if (shouldUseInterpreterEntrypoint != nullptr) {
//            origin_ShouldUseInterpreterEntrypoint = reinterpret_cast<bool (*)(ArtMethod *,
//                                                                              const void *)>(hook_native(
//                    shouldUseInterpreterEntrypoint,
//                    reinterpret_cast<void *>(replace_ShouldUseInterpreterEntrypoint)));

            MSHookFunction(shouldUseInterpreterEntrypoint,
                           (void *) replace_ShouldUseInterpreterEntrypoint,
                           (void **) &origin_ShouldUseInterpreterEntrypoint);
        }
    }

    runtime_instance_ = *reinterpret_cast<void **>(getSymCompat(art_lib_path,
                                                                "_ZN3art7Runtime9instance_E"));

}

bool canCompile() {
    if (SDK_INT >= ANDROID_R)
        return false;
    if (getGlobalJitCompiler() == nullptr) {
        ALOGE("JIT not init!");
        return false;
    }
    JNIEnv *env;
    jvm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
    return getBooleanFromJava(env, sandHookConfigClass, "compiler");
}

static inline uint32_t ReadInt32(void *pointer) {
    return *((uint32_t *) pointer);
}


bool compileMethod(void *artMethod, void *thread) {
    if (jitCompilerHandle == nullptr)
        return false;
    if (!canCompile()) return false;
    if (SDK_INT >= ANDROID_Q) {
        if (jitCompileMethodQ == nullptr) {
            return false;
        }
        bool ret;
        int old_flag_and_state = ReadInt32(thread);
        ret = jitCompileMethodQ(jitCompilerHandle, artMethod, thread, false, false);
        memcpy(thread, &old_flag_and_state, 4);
        return ret;
    } else {
        if (jitCompileMethod == nullptr) {
            return false;
        }
        bool ret;
        int old_flag_and_state = ReadInt32(thread);
        ret = jitCompileMethod(jitCompilerHandle, artMethod, thread, false);
        memcpy(thread, &old_flag_and_state, 4);
        return ret;
    }
}

void suspendVM() {
    if (innerSuspendVM == nullptr || innerResumeVM == nullptr)
        return;
//#ifdef RATEL_BUILD_TYPE_DEBUG
//    ALOGI("begin suspendVM");
//#endif
    innerSuspendVM();
//#ifdef RATEL_BUILD_TYPE_DEBUG
//    ALOGI("end of suspendVM");
//#endif
}

void resumeVM() {
    if (innerSuspendVM == nullptr || innerResumeVM == nullptr)
        return;
//#ifdef RATEL_BUILD_TYPE_DEBUG
//    ALOGI("begin ResumeVM");
//#endif
    innerResumeVM();
//#ifdef RATEL_BUILD_TYPE_DEBUG
//    ALOGI("end of ResumeVM");
//#endif
}

bool canGetObject() {
    return addWeakGlobalRef != nullptr;
}

void *getCurrentThread() {
    return __get_tls()[TLS_SLOT_ART_THREAD];
}

jobject getJavaObject(JNIEnv *env, void *thread, void *address) {

    if (addWeakGlobalRef == nullptr)
        return nullptr;

    jobject object = addWeakGlobalRef(jvm, thread, address);
    if (object == nullptr)
        return nullptr;

    jobject result = env->NewLocalRef(object);
    env->DeleteWeakGlobalRef(object);

    return result;
}

art::jit::JitCompiler *getGlobalJitCompiler() {
    if (SDK_INT < ANDROID_N)
        return nullptr;
    if (globalJitCompileHandlerAddr == nullptr)
        return nullptr;
    return *globalJitCompileHandlerAddr;
}

art::CompilerOptions *getCompilerOptions(art::jit::JitCompiler *compiler) {
    if (compiler == nullptr)
        return nullptr;
    return compiler->compilerOptions.get();
}

art::CompilerOptions *getGlobalCompilerOptions() {
    return getCompilerOptions(getGlobalJitCompiler());
}

bool disableJitInline(art::CompilerOptions *compilerOptions) {
    if (compilerOptions == nullptr)
        return false;
    size_t originOptions = compilerOptions->getInlineMaxCodeUnits();
    //maybe a real inlineMaxCodeUnits
    if (originOptions > 0 && originOptions <= 1024) {
        compilerOptions->setInlineMaxCodeUnits(0);
        return true;
    } else {
        return false;
    }
}

void *getInterpreterBridge(bool isNative) {


    SandHook::ElfImg libart(art_lib_path);
    if (isNative) {
        return reinterpret_cast<void *>(libart.getSymbAddress("art_quick_generic_jni_trampoline"));
    } else {
        return reinterpret_cast<void *>(libart.getSymbAddress("art_quick_to_interpreter_bridge"));
    }
}

//to replace jit_update_option
void fake_jit_update_options(void *handle) {
    //do nothing
    ALOGW("android q: art request update compiler options");
}

bool replaceUpdateCompilerOptionsQ() {
    if (SDK_INT < ANDROID_Q)
        return false;
    if (origin_jit_update_options == nullptr
        || *origin_jit_update_options == nullptr)
        return false;
    *origin_jit_update_options = fake_jit_update_options;
    return true;
}

bool forceProcessProfiles() {
    if (profileSaver_ForceProcessProfiles == nullptr)
        return false;
    profileSaver_ForceProcessProfiles();
    return true;
}


void replaceFixupStaticTrampolines(void *thiz, void *clazz_ptr) {
    backup_fixup_static_trampolines(thiz, clazz_ptr);
    if (class_init_callback) {
        class_init_callback(clazz_ptr);
    }
}

void *replaceMarkClassInitialized(void *thiz, void *self, uint32_t *clazz_ptr) {
    auto result = backup_mark_class_initialized(thiz, self, clazz_ptr);
    if (class_init_callback) {
        class_init_callback(reinterpret_cast<void *>(*clazz_ptr));
    }
    return result;
}

void replaceUpdateMethodsCode(void *thiz, ArtMethod *artMethod, const void *quick_code) {
    if (SandHook::TrampolineManager::get().methodHooked(artMethod)) {
        return; //skip
    }
    backup_update_methods_code(thiz, artMethod, quick_code);
}

void MakeInitializedClassVisibilyInitialized(void *self) {
    if (make_initialized_classes_visibly_initialized_) {
#ifdef __LP64__
        constexpr size_t OFFSET_classlinker = 472;
#else
        constexpr size_t OFFSET_classlinker = 276;
#endif
        void *thiz = *reinterpret_cast<void **>(
                reinterpret_cast<size_t>(runtime_instance_) + OFFSET_classlinker);
        make_initialized_classes_visibly_initialized_(thiz, self, true);
    }
}

bool hookClassInit(void(*callback)(void *)) {
    if (SDK_INT >= ANDROID_R) {
        void *symMarkClassInitialized = getSymCompat(art_lib_path,
                                                     "_ZN3art11ClassLinker20MarkClassInitializedEPNS_6ThreadENS_6HandleINS_6mirror5ClassEEE");
        if (symMarkClassInitialized == nullptr) {
            return false;
        }

        MSHookFunction(
                symMarkClassInitialized, (void *) replaceMarkClassInitialized,
                reinterpret_cast<void **>(&backup_mark_class_initialized));

        // Prevent hooked method from resetting entrypoint by visibilyInitialized
        void *symUpdateMethodsCode = getSymCompat(art_lib_path,
                                                  "_ZN3art15instrumentation15Instrumentation21UpdateMethodsCodeImplEPNS_9ArtMethodEPKv");
        if (symUpdateMethodsCode == nullptr) {
            return false;
        }

        MSHookFunction(
                symUpdateMethodsCode, (void *) replaceUpdateMethodsCode,
                reinterpret_cast<void **>(&backup_update_methods_code));

        make_initialized_classes_visibly_initialized_ = reinterpret_cast<void *(*)(void *, void *,
                                                                                   bool)>(
                getSymCompat(art_lib_path,
                             "_ZN3art11ClassLinker40MakeInitializedClassesVisiblyInitializedEPNS_6ThreadEb"));

        if (backup_mark_class_initialized && backup_update_methods_code) {
            ALOGE("hook symMarkClassInitialized success");
            class_init_callback = callback;
            return true;
        } else {
            ALOGE("hook symMarkClassInitialized failed");
            return false;
        }
    } else {
        void *symFixupStaticTrampolines = getSymCompat(art_lib_path,
                                                       "_ZN3art11ClassLinker22FixupStaticTrampolinesENS_6ObjPtrINS_6mirror5ClassEEE");
        if (symFixupStaticTrampolines == nullptr) {
            //huawei lon-al00 android 7.0 api level 24
            symFixupStaticTrampolines = getSymCompat(art_lib_path,
                                                     "_ZN3art11ClassLinker22FixupStaticTrampolinesEPNS_6mirror5ClassE");
        }
        if (symFixupStaticTrampolines == nullptr) {
            ALOGE("can not find symFixupStaticTrampolines symbol,art_lib_path:%s", art_lib_path);
            return false;
        }

        MSHookFunction(
                symFixupStaticTrampolines, (void *) replaceFixupStaticTrampolines,
                reinterpret_cast<void **>(&backup_fixup_static_trampolines));
        if (backup_fixup_static_trampolines) {
            class_init_callback = callback;
            ALOGE("hook symFixupStaticTrampolines success");
            return true;
        } else {
            ALOGE("hook symFixupStaticTrampolines failed");
            return false;
        }
    }
}

JNIEnv *attachAndGetEvn() {
    JNIEnv *env = getEnv();
    if (env == nullptr) {
        jvm->AttachCurrentThread(&env, nullptr);
    }
    return env;
}

static bool isIndexId(jmethodID mid) {
    return (reinterpret_cast<uintptr_t>(mid) % 2) != 0;
}

ArtMethod *getArtMethod(jmethodID jmethodId) {
    if (SDK_INT >= ANDROID_R && isIndexId(jmethodId)) {
        if (origin_DecodeArtMethodId == nullptr) {
            return reinterpret_cast<ArtMethod *>(jmethodId);
        }
        return origin_DecodeArtMethodId(jniIdManager, jmethodId);
    } else {
        return reinterpret_cast<ArtMethod *>(jmethodId);
    }
}


volatile bool hasHookedDex2oat = false;

class DisableDex2OatHandler : public ExecveChainHandler {
protected:
    bool beforeExeceve(ExecveParam *execveParam) override {
        if (strstr(execveParam->pathname, "dex2oat")) {
            ALOGE("skip dex2oat!");
            execveParam->ret = -1;
            execveParam->returnEarly = true;
            return false;
        }
        return true;
    }
};

class DisableInlineHandler : public ExecveChainHandler {
protected:
    bool beforeExeceve(ExecveParam *execveParam) override {
        if (strstr(execveParam->pathname, "dex2oat")) {
            if (SDK_INT >= ANDROID_N && isSandHooker(execveParam->argv)) {
                ALOGE("skip dex2oat!");
                execveParam->ret = -1;
                execveParam->returnEarly = true;
                return false;
            }
            char **new_args = build_new_argv(execveParam->argv);
            ALOGE("dex2oat by disable inline!");
            execveParam->argv = new_args;
        }
        return true;
    }

    void afterExeceve(ExecveParam originExecveParam, ExecveParam afterExecveParam) override {
        if (originExecveParam.argv != afterExecveParam.argv) {
            free(afterExecveParam.argv);
        }
    }
};


bool hookDex2oat(bool disableDex2oat) {
    if (hasHookedDex2oat)
        return false;

    hasHookedDex2oat = true;

    ExecveChainHandler *execveChainHandler = disableDex2oat
                                             ? reinterpret_cast<ExecveChainHandler *>(new DisableDex2OatHandler())
                                             : reinterpret_cast<ExecveChainHandler *>(new DisableInlineHandler());
    register_execve_handler(execveChainHandler);
    return true;
}
}
