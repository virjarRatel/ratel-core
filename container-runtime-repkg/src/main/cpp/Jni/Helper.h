//
// VirtualApp Native Project
//

#ifndef NDK_HELPER
#define NDK_HELPER

#include <arch/arch.h>
#include "VAJni.h"
#include <type_traits>

#define RoundUpToPtrSize(x) (x + BYTE_POINT - 1 - ((x + BYTE_POINT - 1) & (BYTE_POINT - 1)))

class ScopeUtfString {
public:
    ScopeUtfString(jstring j_str);

    const char *c_str() {
        return _c_str;
    }

    ~ScopeUtfString();

private:
    jstring _j_str;
    const char *_c_str;
};


bool end_with(const char *input, const char *suffix);

bool start_with(const char *input, const char *preffix);

//path equal and ignore the last slash
bool path_equal(const char *a, const char *b);

Size getAddressFromJava(JNIEnv *env, const char *className, const char *fieldName);

Size getAddressFromJavaByCallMethod(JNIEnv *env, const char *className, const char *methodName);

jint getIntFromJava(JNIEnv *env, const char *className, const char *fieldName);

bool getBooleanFromJava(JNIEnv *env, const char *className, const char *fieldName);

bool munprotect(size_t addr, size_t len);

bool flushCacheExt(Size addr, Size len);


void printJavaStackTrace(const char *msg);

bool is_mapped(const char *path);

#endif //NDK_HELPER
