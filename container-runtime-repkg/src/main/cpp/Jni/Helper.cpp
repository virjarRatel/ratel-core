
#include <jni.h>
#include <bits/sysconf.h>
#include <sys/mman.h>
#include <zconf.h>
#include "Helper.h"

ScopeUtfString::ScopeUtfString(jstring j_str) {
    _j_str = j_str;
    _c_str = getEnv()->GetStringUTFChars(j_str, nullptr);
}

ScopeUtfString::~ScopeUtfString() {
    getEnv()->ReleaseStringUTFChars(_j_str, _c_str);
}

bool end_with(const char *input, const char *suffix) {
    int input_length = static_cast<int>(strlen(input));
    int suffix_length = static_cast<int>(strlen(suffix));

    if (input_length < suffix_length) {
        return false;
    }

    for (int i = 0; i < suffix_length; i++) {
        if (input[input_length - i - 1] != suffix[suffix_length - i - 1]) {
            return false;
        }
    }
    return true;
}

bool path_equal(const char *a, const char *b) {
    int i = 0;
    while (true) {
        if (a[i] == '\0') {
            if (b[i] == '\0') {
                return true;
            }
            if (b[i] == '/' && b[i + 1] == '\0') {
                return true;
            }
        } else if (b[i] == '\0') {
            if (a[i] == '/' && a[i + 1] == '\0') {
                return true;
            }
        }
        if (a[i] != b[i]) {
            return false;
        }

        i++;
    }
}

bool start_with(const char *input, const char *preffix) {
    int i = 0;
    while (true) {
        if (preffix[i] == '\0') {
            return true;
        }
        if (input[i] == '\0') {
            return false;
        }

        if (input[i] != preffix[i]) {
            return false;
        }
        i++;
    }
}

Size getAddressFromJava(JNIEnv *env, const char *className, const char *fieldName) {
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        printf("find class error !");
        return 0;
    }
    jfieldID id = env->GetStaticFieldID(clazz, fieldName, "J");
    if (id == nullptr) {
        printf("find field error !");
        return 0;
    }
    return static_cast<Size>(env->GetStaticLongField(clazz, id));
}

Size getAddressFromJavaByCallMethod(JNIEnv *env, const char *className, const char *methodName) {
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        printf("find class error !");
        return 0;
    }
    jmethodID id = env->GetStaticMethodID(clazz, methodName, "()J");
    if (id == nullptr) {
        printf("find field error !");
        return 0;
    }
    return static_cast<Size>(env->CallStaticLongMethodA(clazz, id, nullptr));
}

jint getIntFromJava(JNIEnv *env, const char *className, const char *fieldName) {
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        printf("find class error !");
        return 0;
    }
    jfieldID id = env->GetStaticFieldID(clazz, fieldName, "I");
    if (id == nullptr) {
        printf("find field error !");
        return 0;
    }
    return env->GetStaticIntField(clazz, id);
}

bool getBooleanFromJava(JNIEnv *env, const char *className, const char *fieldName) {
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        printf("find class error !");
        return false;
    }
    jfieldID id = env->GetStaticFieldID(clazz, fieldName, "Z");
    if (id == nullptr) {
        printf("find field error !");
        return false;
    }
    return env->GetStaticBooleanField(clazz, id);
}

bool munprotect(size_t addr, size_t len) {
    long pagesize = sysconf(_SC_PAGESIZE);
    unsigned alignment = (unsigned) ((unsigned long long) addr % pagesize);
    int i = mprotect((void *) (addr - alignment), (size_t) (alignment + len),
                     PROT_READ | PROT_WRITE | PROT_EXEC);
    return i != -1;
}

bool flushCacheExt(Size addr, Size len) {
#if defined(__arm__)
    int i = cacheflush(addr, addr + len, 0);
    return i != -1;
#elif defined(__aarch64__)
    char *begin = reinterpret_cast<char *>(addr);
    __builtin___clear_cache(begin, begin + len);
#endif
    return true;
}

void printJavaStackTrace(const char *msg) {
    //public static boolean printJavaStackTrace(String msg) {
    JNIEnv *jniEnv = ensureEnvCreated();
    jmethodID printJavaStackTraceMethod = jniEnv->GetStaticMethodID(nativeEngineClass,
                                                                    "printJavaStackTrace",
                                                                    "(Ljava/lang/String;)V");
    jstring jMsg = nullptr;
    if (msg != nullptr) {
        jMsg = jniEnv->NewStringUTF(msg);
    }
    jniEnv->CallStaticObjectMethod(nativeEngineClass, printJavaStackTraceMethod, jMsg);
    if (msg != nullptr) {
        jniEnv->ReleaseStringUTFChars(jMsg, msg);
    }
}

bool is_mapped(const char *path) {
    FILE *maps = fopen("/proc/self/maps", "r");
    if (!maps) {
        return false;
    }
    bool found = false;
    char buff[256];
    while (fgets(buff, sizeof(buff), maps)) {
        if ((strstr(buff, "r-xp") || strstr(buff, "r--p")) && strstr(buff, path)) {
            found = true;
            break;
        }
    }

    fclose(maps);
    return found;
}
