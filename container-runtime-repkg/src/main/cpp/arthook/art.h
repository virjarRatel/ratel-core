//
// Created by 邓维佳 on 2019-07-30.
//

#ifndef RATEL2_ART_H
#define RATEL2_ART_H


//7.0 - 10.0
#include <jni.h>
#include "arch/arch.h"
#include <memory>

#define GCRoot uint32_t

namespace art {
    class CompilerOptions {
    public:
        void *compiler_filter_;
        size_t huge_method_threshold_;
        size_t large_method_threshold_;
        size_t small_method_threshold_;
        size_t tiny_method_threshold_;
        size_t num_dex_methods_threshold_;
        size_t inline_depth_limit_;
        size_t inline_max_code_units_;

        size_t getInlineMaxCodeUnits();

        bool setInlineMaxCodeUnits(size_t units);

    };


    namespace jit {

        //7.0 - 9.0
        class JitCompiler {
        public:
            virtual ~JitCompiler();

            std::unique_ptr<art::CompilerOptions> compilerOptions;
        };

        class Jit {
        public:
            //void* getCompilerOptions();
        };


    };

    class Runtime {

    public:
        jit::Jit *getJit();
    };

    namespace mirror {
        class Object {
        public:
        };

        class Class : public Object {
        public:
        };

        class ArtField {
        public:
        };

        class ArtMethod {
        public:

            bool isAbstract();

            bool isNative();

            bool isStatic();

            bool isCompiled();

            bool isThumbCode();

            void setAccessFlags(uint32_t flags);

            void disableCompilable();

            void tryDisableInline();

            void disableInterpreterForO();
            void disableFastInterpreterForQ();
            void setPrivate();

            void setStatic();

            void setNative();

            void setQuickCodeEntry(void *entry);

            void setJniCodeEntry(void *entry);

            void setInterpreterCodeEntry(void *entry);

            void setDexCacheResolveList(void *list);

            void setDexCacheResolveItem(uint32_t index, void *item);

            void setDeclaringClass(GCRoot classPtr);

            void setHotnessCount(uint16_t count);

            void *getQuickCodeEntry();

            void *getInterpreterCodeEntry();

            uint32_t getAccessFlags();

            uint32_t getDexMethodIndex();

            uint32_t getDexCodeItemIndex();

            GCRoot getDeclaringClass();

            uint16_t getHotnessCount();

            bool compile(JNIEnv *env);

            bool deCompile();

            void flushCache();

            void backup(ArtMethod *backup);

            static Size size();

        };

    }


}


#endif //RATEL2_ART_H
