//
// VirtualApp Native Project
//

#ifndef NDK_HOOK_H
#define NDK_HOOK_H


#include <string>
#include <map>
#include <list>
#include <jni.h>
#include <dlfcn.h>
#include <stddef.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/syscall.h>

#include "Jni/Helper.h"


#define HOOK_SYMBOL(handle, func) hook_function(handle, #func, (void*) new_##func, (void**) &orig_##func)
#define HOOK_DEF(ret, func, ...) \
  ret (*orig_##func)(__VA_ARGS__); \
  ret new_##func(__VA_ARGS__)

#define HOOK_SYSCALL(syscall_name) \
case __NR_##syscall_name: \
    MSHookFunction(func, (void *) new_##syscall_name, (void **) &orig_##syscall_name); \
    pass++; \
    break;

#define HOOK_SYSCALL_(syscall_name) \
case __NR_##syscall_name: \
    MSHookFunction(func, (void *) new___##syscall_name, (void **) &orig___##syscall_name); \
    pass++; \
    break;

namespace IOUniformer {

    //在execve函数中使用，提供在子进程中hook的功能
    void init_env_before_all();

    void startUniformer(int api_level, int preview_api_level, const char *so_path,
                        const char *so_path_64);

    void relocate(const char *orig_path, const char *new_path);

    void clearRelocateConfig();

    void whitelist(const char *path);

    const char *query(const char *orig_path, char *const buffer, const size_t size);

    const char *reverse(const char *redirected_path, char *const buffer, const size_t size);

    void forbid(const char *path);

    void readOnly(const char *path);

    void addMockStat(const char *path, jlong last_modified);
}

#endif //NDK_HOOK_H
