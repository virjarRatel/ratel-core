#ifndef ZIP_ALIGN_UTILS_LOG_H
#define ZIP_ALIGN_UTILS_LOG_H

#include <android/log.h>

#define ZIP_LOG_TAG "RATEL_ZIP_ALIGN"

#define ALOGV(...) __android_log_print(ANDROID_LOG_INFO, ZIP_LOG_TAG ,__VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, ZIP_LOG_TAG ,__VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, ZIP_LOG_TAG ,__VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, ZIP_LOG_TAG ,__VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, ZIP_LOG_TAG ,__VA_ARGS__)

//from aosp/system/core/include/log/log_main.h

/*
 * Use __VA_ARGS__ if running a static analyzer,
 * to avoid warnings of unused variables in __VA_ARGS__.
 * Use constexpr function in C++ mode, so these macros can be used
 * in other constexpr functions without warning.
 */
#ifdef __clang_analyzer__
#ifdef __cplusplus
extern "C++" {
template <typename... Ts>
constexpr int __fake_use_va_args(Ts...) {
  return 0;
}
}
#else
extern int __fake_use_va_args(int, ...);
#endif /* __cplusplus */
#define __FAKE_USE_VA_ARGS(...) ((void)__fake_use_va_args(0, ##__VA_ARGS__))
#else
#define __FAKE_USE_VA_ARGS(...) ((void)(0))
#endif /* __clang_analyzer__ */

/* Returns 2nd arg.  Used to substitute default value if caller's vararg list
 * is empty.
 */
#define __android_second(dummy, second, ...) second

/* If passed multiple args, returns ',' followed by all but 1st arg, otherwise
 * returns nothing.
 */
#define __android_rest(first, ...) , ##__VA_ARGS__

#define android_printAssert(cond, tag, ...)                     \
  __android_log_assert(cond, tag,                               \
                       __android_second(0, ##__VA_ARGS__, NULL) \
                           __android_rest(__VA_ARGS__))
/*
 * Log a fatal error.  If the given condition fails, this stops program
 * execution like a normal assertion, but also generating the given message.
 * It is NOT stripped from release builds.  Note that the condition test
 * is -inverted- from the normal assert() semantics.
 */
#ifndef LOG_ALWAYS_FATAL_IF
#define LOG_ALWAYS_FATAL_IF(cond, ...)                                                    \
  ((__predict_false(cond)) ? (__FAKE_USE_VA_ARGS(__VA_ARGS__),                            \
                              ((void)android_printAssert(#cond, ZIP_LOG_TAG, ##__VA_ARGS__))) \
                           : ((void)0))
#endif

#ifndef LOG_ALWAYS_FATAL
#define LOG_ALWAYS_FATAL(...) \
  (((void)android_printAssert(NULL, ZIP_LOG_TAG, ##__VA_ARGS__)))
#endif


#ifndef ALOGW_IF
#define ALOGW_IF(cond, ...)                                                            \
  ((__predict_false(cond))                                                             \
       ? (__FAKE_USE_VA_ARGS(__VA_ARGS__), (void)ALOGW(ZIP_LOG_TAG, __VA_ARGS__)) \
       : ((void)0))
#endif

#ifndef LOG_FATAL_IF
#define LOG_FATAL_IF(cond, ...) LOG_ALWAYS_FATAL_IF(cond, ##__VA_ARGS__)
#endif
/*
 * Assertion that generates a log message when the assertion fails.
 * Stripped out of release builds.  Uses the current LOG_TAG.
 */
#ifndef ALOG_ASSERT
#define ALOG_ASSERT(cond, ...) LOG_FATAL_IF(!(cond), ##__VA_ARGS__)
#endif

#endif