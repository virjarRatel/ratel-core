//
// Created by swift on 2019/2/3.
//

#include "cast.h"
#include "VAJni.h"
#include "native_api.h"

extern int SDK_INT;

namespace SandHook {

    class CastDexCacheResolvedMethods : public ArrayMember<art::mirror::ArtMethod, void *> {
    protected:
        Size calOffset(JNIEnv *jniEnv, art::mirror::ArtMethod *p) override {
            if (SDK_INT >= ANDROID_P)
                return getParentSize() + 1;
            int offset = 0;
            Size addr = getAddressFromJava(jniEnv, sandHookMethodResolverClass,
                                           "resolvedMethodsAddress");
            if (addr != 0) {
                offset = findOffset(p, getParentSize(), 2, addr);
                if (offset >= 0) {
                    return static_cast<Size>(offset);
                }
            }
            if (SDK_INT == ANDROID_M) {
                return 4;
            } else if (SDK_INT >= ANDROID_L && SDK_INT <= ANDROID_L2) {
                return 4 * 3;
            }
            return getParentSize() + 1;
        }

    public:
        Size arrayStart(art::mirror::ArtMethod *parent) override {
            void *p = IMember<art::mirror::ArtMethod, void *>::get(parent);
            if (SDK_INT <= ANDROID_M) {
                return reinterpret_cast<Size>(p) + 4 * 3;
            } else {
                return reinterpret_cast<Size>(p);
            }
        }

    };

    class CastEntryPointFromInterpreter : public IMember<art::mirror::ArtMethod, void *> {
    protected:
        Size calOffset(JNIEnv *jniEnv, art::mirror::ArtMethod *p) override {
            if (SDK_INT == ANDROID_L2) {
                return RoundUpToPtrSize(4 * 7 + 4 * 2);
            } else if (SDK_INT == ANDROID_M) {
                return getParentSize() - 3 * BYTE_POINT;
            } else if (SDK_INT <= ANDROID_L) {
                Size addr = getAddressFromJava(jniEnv, sandHookMethodResolverClass,
                                               "entryPointFromInterpreter");
                int offset = 0;
                if (addr != 0) {
                    offset = findOffset(p, getParentSize(), 2, addr);
                    if (offset >= 0) {
                        return static_cast<Size>(offset);
                    }
                }
                return getParentSize() - 4 * 8 - 4 * 4;
            } else
                return getParentSize() + 1;
        }
    };

    class CastEntryPointQuickCompiled : public IMember<art::mirror::ArtMethod, void *> {
    protected:
        Size calOffset(JNIEnv *jniEnv, art::mirror::ArtMethod *p) override {
            if (SDK_INT >= ANDROID_M) {
                return getParentSize() - BYTE_POINT;
            } else if (SDK_INT <= ANDROID_L) {
                Size addr = getAddressFromJava(jniEnv, sandHookMethodResolverClass,
                                               "entryPointFromCompiledCode");
                int offset = 0;
                if (addr != 0) {
                    offset = findOffset(p, getParentSize(), 2, addr);
                    if (offset >= 0) {
                        return static_cast<Size>(offset);
                    }
                }
                return getParentSize() - 4 - 2 * BYTE_POINT;
            } else {
                return SandHook::CastArtMethod::entryPointFromInterpreter->getOffset() +
                       2 * BYTE_POINT;
            }
        }
    };

    class CastEntryPointFromJni : public IMember<art::mirror::ArtMethod, void *> {
    protected:
        Size calOffset(JNIEnv *jniEnv, art::mirror::ArtMethod *p) override {
            if (SDK_INT >= ANDROID_L2 && SDK_INT <= ANDROID_N) {
                return getParentSize() - 2 * BYTE_POINT;
            } else {
                return getParentSize() - 8 * 2 - 4 * 4;
            }
        }
    };


    class CastAccessFlag : public IMember<art::mirror::ArtMethod, uint32_t> {
    protected:
        Size calOffset(JNIEnv *jniEnv, art::mirror::ArtMethod *p) override {
            uint32_t accessFlag = getIntFromJava(jniEnv, sandSandHookClass,
                                                 "testAccessFlag");
            if (accessFlag == 0) {
                accessFlag = 524313;
                //kAccPublicApi
                if (SDK_INT >= ANDROID_Q) {
                    accessFlag |= 0x10000000;
                }
            }
            int offset = findOffset(p, getParentSize(), 2, accessFlag);
            if (offset < 0) {
                if (SDK_INT >= ANDROID_N) {
                    return 4;
                } else if (SDK_INT == ANDROID_L2) {
                    return 20;
                } else if (SDK_INT == ANDROID_L) {
                    return 56;
                } else {
                    return getParentSize() + 1;
                }
            } else {
                return static_cast<size_t>(offset);
            }
        }
    };

    class CastShadowClass : public IMember<art::mirror::ArtMethod, GCRoot> {
    protected:
        Size calOffset(JNIEnv *jniEnv, art::mirror::ArtMethod *p) override {
            if (SDK_INT < ANDROID_N)
                return getParentSize() + 1;
            return 0;
        }
    };


    class CastDexMethodIndex : public IMember<art::mirror::ArtMethod, uint32_t> {
    protected:
        Size calOffset(JNIEnv *jniEnv, art::mirror::ArtMethod *p) override {
            if (SDK_INT >= ANDROID_P) {
                return CastArtMethod::accessFlag->getOffset()
                       + CastArtMethod::accessFlag->size()
                       + sizeof(uint32_t);
            }
            int offset = 0;
            jint index = getIntFromJava(jniEnv, sandHookMethodResolverClass,
                                        "dexMethodIndex");
            if (index != 0) {
                offset = findOffset(p, getParentSize(), 2, static_cast<uint32_t>(index));
                if (offset >= 0) {
                    return static_cast<Size>(offset);
                }
            }
            return getParentSize() + 1;
        }
    };

    class CastDexCodeItemIndex : public IMember<art::mirror::ArtMethod, uint32_t> {
    protected:
        Size calOffset(JNIEnv *jniEnv, art::mirror::ArtMethod *p) override {
            if (SDK_INT >= ANDROID_P) {
                return CastArtMethod::accessFlag->getOffset()
                       + CastArtMethod::accessFlag->size();
            }
            return CastArtMethod::dexMethodIndex->getOffset()
                   - CastArtMethod::dexMethodIndex->size();
        }
    };

    class CastHotnessCount : public IMember<art::mirror::ArtMethod, uint16_t> {
    protected:
        Size calOffset(JNIEnv *jniEnv, art::mirror::ArtMethod *p) override {
            if (SDK_INT <= ANDROID_N)
                return getParentSize() + 1;
            return CastArtMethod::dexMethodIndex->getOffset()
                   + CastArtMethod::dexMethodIndex->size()
                   + sizeof(uint16_t);
        }
    };


    void CastArtMethod::init(JNIEnv *env) {
        //init ArtMethodSize
        jclass sizeTestClass = env->FindClass(artMethodSizeTestCLass);
        jmethodID artMethod1 = env->GetStaticMethodID(sizeTestClass, "method1", "()V");
        jmethodID artMethod2 = env->GetStaticMethodID(sizeTestClass, "method2", "()V");

        env->CallStaticVoidMethod(sizeTestClass, reinterpret_cast<jmethodID>(artMethod1));

        std::atomic_thread_fence(std::memory_order_acquire);

        art::mirror::ArtMethod *m1 = getArtMethod(artMethod1);
        art::mirror::ArtMethod *m2 = getArtMethod(artMethod2);

        size = m2 - m1;

        //init Members

        accessFlag = new CastAccessFlag();
        accessFlag->init(env, m1, size);

        entryPointFromInterpreter = new CastEntryPointFromInterpreter();
        entryPointFromInterpreter->init(env, m1, size);

        entryPointQuickCompiled = new CastEntryPointQuickCompiled();
        entryPointQuickCompiled->init(env, m1, size);

        dexMethodIndex = new CastDexMethodIndex();
        dexMethodIndex->init(env, m1, size);

        dexCodeItemIndex = new CastDexCodeItemIndex();
        dexCodeItemIndex->init(env, m1, size);

        dexCacheResolvedMethods = new CastDexCacheResolvedMethods();
        dexCacheResolvedMethods->init(env, m1, size);

        declaringClass = new CastShadowClass();
        declaringClass->init(env, m1, size);


        hotnessCount = new CastHotnessCount();
        hotnessCount->init(env, m1, size);

        jclass neverCallTestClass = env->FindClass(sandClassNeverCallClass);


        art::mirror::ArtMethod *neverCall = getArtMethod(env->GetMethodID(
                neverCallTestClass, "neverCall", "()V"));
        art::mirror::ArtMethod *neverCall2 = getArtMethod(env->GetMethodID(
                neverCallTestClass, "neverCall2", "()V"));

        bool beAot =
                entryPointQuickCompiled->get(neverCall) != entryPointQuickCompiled->get(neverCall2);
        if (beAot) {
            quickToInterpreterBridge = getInterpreterBridge(false);
            if (quickToInterpreterBridge == nullptr) {
                quickToInterpreterBridge = entryPointQuickCompiled->get(neverCall);
                canGetInterpreterBridge = false;
            }
        } else {
            quickToInterpreterBridge = entryPointQuickCompiled->get(neverCall);
        }


        art::mirror::ArtMethod *neverCallNative = getArtMethod(env->GetMethodID(
                neverCallTestClass, "neverCallNative", "()V"));
        art::mirror::ArtMethod *neverCallNative2 = getArtMethod(env->GetMethodID(
                neverCallTestClass, "neverCallNative2", "()V"));


        beAot = entryPointQuickCompiled->get(neverCallNative) !=
                entryPointQuickCompiled->get(neverCallNative2);
        if (beAot) {
            genericJniStub = getInterpreterBridge(true);
            if (genericJniStub == nullptr) {
                genericJniStub = entryPointQuickCompiled->get(neverCallNative);
                canGetJniBridge = false;
            }
        } else {
            genericJniStub = entryPointQuickCompiled->get(neverCallNative);
        }

        art::mirror::ArtMethod *neverCallStatic = getArtMethod(env->GetStaticMethodID(
                neverCallTestClass, "neverCallStatic", "()V"));
        staticResolveStub = entryPointQuickCompiled->get(neverCallStatic);

    }

    void CastArtMethod::copy(art::mirror::ArtMethod *from, art::mirror::ArtMethod *to) {
        memcpy(to, from, size);
    }

    Size CastArtMethod::size = 0;
    IMember<art::mirror::ArtMethod, void *> *CastArtMethod::entryPointQuickCompiled = nullptr;
    IMember<art::mirror::ArtMethod, void *> *CastArtMethod::entryPointFromInterpreter = nullptr;
    ArrayMember<art::mirror::ArtMethod, void *> *CastArtMethod::dexCacheResolvedMethods = nullptr;
    IMember<art::mirror::ArtMethod, uint32_t> *CastArtMethod::dexMethodIndex = nullptr;
    IMember<art::mirror::ArtMethod, uint32_t> *CastArtMethod::accessFlag = nullptr;
    IMember<art::mirror::ArtMethod, GCRoot> *CastArtMethod::declaringClass = nullptr;
    IMember<art::mirror::ArtMethod, uint16_t> *CastArtMethod::hotnessCount = nullptr;
    IMember<art::mirror::ArtMethod, uint32_t> *CastArtMethod::dexCodeItemIndex = nullptr;
    void *CastArtMethod::quickToInterpreterBridge = nullptr;
    void *CastArtMethod::genericJniStub = nullptr;
    void *CastArtMethod::staticResolveStub = nullptr;
    bool CastArtMethod::canGetInterpreterBridge = true;
    bool CastArtMethod::canGetJniBridge = true;


    class CastInlineMaxCodeUnits : public IMember<art::CompilerOptions, size_t> {
    protected:
        Size calOffset(JNIEnv *jniEnv, art::CompilerOptions *p) override {
            if (SDK_INT < ANDROID_N)
                return getParentSize() + 1;
            if (SDK_INT >= ANDROID_Q) {
                return BYTE_POINT + 3 * sizeof(size_t);
            }
            if (SDK_INT >= ANDROID_O) {
                return BYTE_POINT + 5 * sizeof(size_t);
            } else {
                return BYTE_POINT + 6 * sizeof(size_t);
            }
        }
    };

    void CastCompilerOptions::init(JNIEnv *jniEnv) {
        inlineMaxCodeUnits->init(jniEnv, nullptr, sizeof(art::CompilerOptions));
    }

    IMember<art::CompilerOptions, size_t> *CastCompilerOptions::inlineMaxCodeUnits = new CastInlineMaxCodeUnits();

}