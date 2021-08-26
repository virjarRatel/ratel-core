//
// Created by 甘尧 on 2019/1/12.
//


#ifndef SANDHOOK_ICAST_H
#define SANDHOOK_ICAST_H

#include <stdint.h>
#include <string.h>
#include <jni.h>
#include "art.h"
#include "arch/arch.h"


namespace SandHook {

    template<typename T>
    class cast {
    public:
        cast(T t) {
            this->origin = t;
        };

        virtual Size getSize() { return sizeof(T); };

    private:
        T origin;
    };

    template<typename PType, typename MType>
    class IMember {
    public:

        virtual void init(JNIEnv *jniEnv, PType *p, Size size) {
            this->parentSize = size;
            offset = calOffset(jniEnv, p);
        }

        Size size() {
            return sizeof(MType);
        }

        virtual Size getOffset() {
            return offset;
        }

        virtual Size getParentSize() {
            return parentSize;
        }

        virtual MType get(PType *p) {
            if (offset > parentSize)
                return (MType) NULL;
            return *reinterpret_cast<MType *>((Size) p + getOffset());
        };

        virtual void set(PType *p, MType t) {
            if (offset > parentSize)
                return;
            memcpy(reinterpret_cast<void *>((Size) p + getOffset()), &t, size());
        };

        template<typename T>
        int findOffset(void *start, size_t len, size_t step, T value) {

            if (nullptr == start) {
                return -1;
            }

            for (int i = 0; i <= len; i += step) {
                T current_value = *reinterpret_cast<T *>((size_t) start + i);
                if (value == current_value) {
                    return i;
                }
            }
            return -1;
        }

    private:
        Size offset = 0;
    protected:
        Size parentSize = 0;

        virtual Size calOffset(JNIEnv *jniEnv, PType *p) = 0;

    };

    template<typename PType, typename ElementType>
    class ArrayMember : public IMember<PType, void *> {
    public:

        virtual void init(JNIEnv *jniEnv, PType *p, Size parentSize) override {
            IMember<PType, void *>::init(jniEnv, p, parentSize);
            elementSize = calElementSize(jniEnv, p);
        }

        virtual Size getElementSize() {
            return elementSize;
        }

        virtual Size arrayStart(PType *parent) {
            void *p = IMember<PType, void *>::get(parent);
            return reinterpret_cast<Size>(p);
        }

        using IMember<PType, void *>::getParentSize;

        virtual void setElement(PType *parent, int position, ElementType elementPoint) {
            Size array = arrayStart(parent);
            memcpy(reinterpret_cast<void *>(array + position * getElementSize()), &elementPoint,
                   getElementSize());
        }

    private:
        Size elementSize = 0;
    protected:
        virtual Size calElementSize(JNIEnv *jniEnv, PType *p) {
            return sizeof(ElementType);
        };
    };

    class CastArtMethod {
    public:
        static Size size;
        static IMember<art::mirror::ArtMethod, void *> *entryPointQuickCompiled;
        static IMember<art::mirror::ArtMethod, void *> *entryPointFromInterpreter;
        static ArrayMember<art::mirror::ArtMethod, void *> *dexCacheResolvedMethods;
        static IMember<art::mirror::ArtMethod, uint32_t> *dexMethodIndex;
        static IMember<art::mirror::ArtMethod, uint32_t> *accessFlag;
        static IMember<art::mirror::ArtMethod, GCRoot> *declaringClass;
        static IMember<art::mirror::ArtMethod, uint16_t> *hotnessCount;
        static IMember<art::mirror::ArtMethod, uint32_t> *dexCodeItemIndex;
        static void *quickToInterpreterBridge;
        static void *genericJniStub;
        static void *staticResolveStub;
        static bool canGetJniBridge;
        static bool canGetInterpreterBridge;

        static void init(JNIEnv *env);

        static void copy(art::mirror::ArtMethod *from, art::mirror::ArtMethod *to);

    };

    class CastCompilerOptions {
    public:
        static void init(JNIEnv *jniEnv);

        static IMember<art::CompilerOptions, size_t> *inlineMaxCodeUnits;
    };

}


#endif //SANDHOOK_ICAST_H