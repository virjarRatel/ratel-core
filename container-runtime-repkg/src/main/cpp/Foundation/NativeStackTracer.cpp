//
// Created by 邓维佳 on 2020/5/26.
//

#include <dlfcn.h>
#include "NativeStackTracer.h"
#include "dlfcn_nougat.h"
#include "Log.h"

NativeStackTracer stackTracer;
IOTracer ioTracer;

NativeStackTracer::NativeStackTracer() {
    const char *libBacktraceSoPath = nullptr;
#if defined(__aarch64__)
    libBacktraceSoPath = "/system/lib64/libbacktrace.so";
#else
    libBacktraceSoPath = "/system/lib/libbacktrace.so";
#endif
    void *libbacktrace = fake_dlopen(libBacktraceSoPath, RTLD_LOCAL);

    if (libbacktrace == nullptr) {
        ALOGE("can not get libbacktrace address,native trace will not work");
        return;
    }
    //; _DWORD __fastcall Backtrace::Create(Backtrace *__hidden this, int, int, BacktraceMap *)
    createBacktraceMap = (Backtrace *(*)(int, int, void *)) fake_dlsym(libbacktrace,
                                                                       "_ZN9Backtrace6CreateEiiP12BacktraceMap");
    if (createBacktraceMap == nullptr) {
        ALOGE("can not get Backtrace::Create symbol: _ZN9Backtrace6CreateEiiP12BacktraceMap");
    }
    fake_dlclose(libbacktrace);
}


int NativeStackTracer::printNativeStackTrace(const char *addition) {
    if (createBacktraceMap == nullptr) {
        return -1;
    }
    Backtrace *t = createBacktraceMap(-1, -1, NULL);
    if (!t) {
        return -1;
    }
    int ret = t->Unwind(0);
    if (!ret) {
        return -1;
    }
    size_t count = t->NumFrames();
    ALOGI("Backtrace ->:\n");
    if (addition != nullptr) {
        ALOGI("Addition ->: %s\n", addition);
    }
    for (size_t i = 0; i < count; i++) {
        std::string line = t->FormatFrameData(i);
        ALOGI("%s\n", line.c_str());
    }
    return 0;
}

void IOTracer::printTraceIfMatch(const char *now_path) {
    std::string nowPath(now_path);

    std::vector<std::string>::iterator it = ioTraceConfig.begin();
    while (it != ioTraceConfig.end()) {
        std::string config_path = *it;
        if (start_with(now_path, config_path.c_str())) {
            ALOGI("hint io trace: %s", now_path);
            // hint
            //stackTracer.printNativeStackTrace(now_path);
            //TODO show native trace
            showJavaStackTraceIfInVm();
            break;
        }
        it++;
    }
}

void IOTracer::showJavaStackTraceIfInVm() {
    // public static void showStackTrace() {
    JNIEnv *env = getEnv();
    if (env == nullptr) {
        return;
    }
    jmethodID showStackTraceMethodId = getEnv()->GetStaticMethodID(nativeBridgeClass,
                                                                   "showStackTrace",
                                                                   "()V");
    getEnv()->CallStaticVoidMethod(nativeBridgeClass, showStackTraceMethodId);
}

void add_io_trace_config(const char *path) {
    char buff[PATH_MAX];
    const char *relocated_path = relocate_path(path, buff, sizeof(buff), false);
    if (relocated_path == nullptr) {
        //??
        return;
    }
    ioTracer.addIOTraceConfig(std::string(path));
}

void print_trace_if_match(const char *now_path) {
    ioTracer.printTraceIfMatch(now_path);
}
