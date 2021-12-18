//
// VirtualApp Native Project
//
#include <unistd.h>
#include <cstdlib>
#include <sys/ptrace.h>
#include <Substrate/CydiaSubstrate.h>
#include <Jni/VAJni.h>
#include <sys/stat.h>
#include <syscall.h>
#include <Foundation/syscall/BinarySyscallFinder.h>
#include <climits>
#include <sys/socket.h>
#include <sys/wait.h>
#include <sys/user.h>
#include <ifaddrs.h>
#include <Substrate/SubstrateHook.h>

#include "IORelocator.h"
#include "SandboxFs.h"
#include "SandboxProperties.h"
#include "canonicalize_md.h"
#include "Symbol.h"
#include "Log.h"
#include "MapsRedirector.h"
#include "ExecveHandler.h"
#include "MacAddressFake.h"
#include <MapsHideHandler.h>
#include <xhook.h>
#include <bytehook.h>

#if defined(__LP64__)
#define LINKER_PATH "/system/bin/linker64"
#else
#define LINKER_PATH "/system/bin/linker"
#endif

void startIOHook(int api_level, bool needHookProperties);

bool need_load_env = true;

bool execve_process = false;
int user_seed = -1;
bool open_mac_address_fake = false;

void startIOHook(int api_level, bool needHookProperties);

char *get_process_name() {
    char *cmdline = (char *) calloc(0x400u, 1u);
    if (cmdline) {
        FILE *file = fopen("/proc/self/cmdline", "r");
        if (file) {
            int count = fread(cmdline, 1u, 0x400u, file);
            if (count) {
                if (cmdline[count - 1] == '\n') {
                    cmdline[count - 1] = '\0';
                }
            }
            fclose(file);
        } else {
            ALOGE("fail open cmdline.");
        }
    }
    return cmdline;
}

void IOUniformer::init_env_before_all() {
    if (!need_load_env) {
        return;
    }
    need_load_env = false;
    char *ld_preload = getenv("LD_PRELOAD");
    if (!ld_preload || !strstr(ld_preload, CORE_SO_NAME)) {
        ALOGW("no LD_PRELOAD defined :%s CORE_SO_NAME:%s", ld_preload, CORE_SO_NAME);
        return;
    }
    execve_process = true;
    char *process_name = get_process_name();
    ALOGI("Start init env : %s", process_name);
    free(process_name);
    char src_key[KEY_MAX];
    char dst_key[KEY_MAX];
    int i = 0;
    while (true) {
        memset(src_key, 0, sizeof(src_key));
        memset(dst_key, 0, sizeof(dst_key));
        sprintf(src_key, "V_REPLACE_ITEM_SRC_%d", i);
        sprintf(dst_key, "V_REPLACE_ITEM_DST_%d", i);
        char *src_value = getenv(src_key);
        if (!src_value) {
            break;
        }
        char *dst_value = getenv(dst_key);
        add_replace_item(src_value, dst_value);
        i++;
    }
    i = 0;
    while (true) {
        memset(src_key, 0, sizeof(src_key));
        sprintf(src_key, "V_KEEP_ITEM_%d", i);
        char *keep_value = getenv(src_key);
        if (!keep_value) {
            break;
        }
        add_keep_item(keep_value);
        i++;
    }
    i = 0;
    while (true) {
        memset(src_key, 0, sizeof(src_key));
        sprintf(src_key, "V_FORBID_ITEM_%d", i);
        char *forbid_value = getenv(src_key);
        if (!forbid_value) {
            break;
        }
        add_forbidden_item(forbid_value);
        i++;
    }
    char *zelda_flag = getenv("ZE_ORIGIN_PKG");
    if (zelda_flag != nullptr) {
        zelda_origin_package_name = strdup(getenv("ZE_ORIGIN_PKG"));
        zelda_now_package_name = strdup(getenv("ZE_NOW_PKG"));
        zelda_origin_package_name_length = strlen(zelda_origin_package_name);
        zelda_now_package_name_length = strlen(zelda_now_package_name);
        enable_zelda = true;
    }

    //    setenv("FP_USER_SEED", api_level_chars, 1);
    //    setenv("FP_MAC_FAKE", open_mac_address_fake ? "1" : "0", 1);
    char *user_seed_chars = getenv("FP_USER_SEED");
    if (user_seed_chars != nullptr) {
        char *stop_string;
        user_seed = static_cast<int>(strtol(user_seed_chars, &stop_string, 10));
    }
    char *mac_fake_chars = getenv("FP_MAC_FAKE");
    if (mac_fake_chars != nullptr) {
        open_mac_address_fake = mac_fake_chars[0] == '1';
    }


    char *api_level_char = getenv("V_API_LEVEL");
    if (api_level_char != nullptr) {
        char *stopstring;
        int api_level = static_cast<int>(strtol(api_level_char, &stopstring, 10));
        startIOHook(api_level, false);
    }
    //子进程需要进行一次maps hide，否则子进程会有泄漏 libratelnative_64.so
    doMapsHide(true);
}

static inline void
hook_function(void *handle, const char *symbol, void *new_func, void **old_func) {
    void *addr = dlsym(handle, symbol);
    if (addr == nullptr) {
        ALOGE("Not found symbol : %s", symbol);
        return;
    }
    MSHookFunction(addr, new_func, old_func);
}

static inline void
hook_function_old(void *handle, const char *symbol, void *new_func, void **old_func) {
    void *addr = dlsym(handle, symbol);
    if (addr == nullptr) {
        ALOGE("Not found symbol : %s", symbol);
        return;
    }
    MSHookFunctionOld(addr, new_func, old_func);
}

void onSoLoaded(const char *name, void *handle);

void IOUniformer::relocate(const char *orig_path, const char *new_path) {
    add_replace_item(orig_path, new_path);
}

void IOUniformer::clearRelocateConfig() {
    clear_replace_item();
}

const char *IOUniformer::query(const char *orig_path, char *const buffer, const size_t size) {
    return relocate_path(orig_path, buffer, size);
}

void IOUniformer::whitelist(const char *_path) {
    add_keep_item(_path);
}

void IOUniformer::forbid(const char *_path) {
    add_forbidden_item(_path);
}

void IOUniformer::readOnly(const char *_path) {
    add_readonly_item(_path);
}

const char *IOUniformer::reverse(const char *_path, char *const buffer, const size_t size) {
    return reverse_relocate_path(_path, buffer, size);
}

void IOUniformer::addMockStat(const char *path, jlong last_modified) {
    add_stat_mock_item(path, last_modified);
}


__BEGIN_DECLS

// int faccessat(int dirfd, const char *pathname, int mode, int flags);
HOOK_DEF(int, faccessat, int dirfd, const char *pathname, int mode, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (relocated_path && !(mode & W_OK && isReadOnly(relocated_path))) {
        return syscall(__NR_faccessat, dirfd, relocated_path, mode, flags);
    }
    errno = EACCES;
    return -1;
}

// int fchmodat(int dirfd, const char *pathname, mode_t mode, int flags);
HOOK_DEF(int, fchmodat, int dirfd, const char *pathname, mode_t mode, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_fchmodat, dirfd, relocated_path, mode, flags);
    }
    errno = EACCES;
    return -1;
}

// int fstatat64(int dirfd, const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstatat64, int dirfd, const char *pathname, struct stat *buf, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        long ret;
#if defined(__arm__) || defined(__i386__)
        ret = syscall(__NR_fstatat64, dirfd, relocated_path, buf, flags);
#else
        ret = syscall(__NR_newfstatat, dirfd, relocated_path, buf, flags);
#endif
        handleMockStat(pathname, buf);
        return ret;
    }
    errno = EACCES;
    return -1;
}

// int kill(pid_t pid, int sig);
HOOK_DEF(int, kill, pid_t pid, int sig) {
    ALOGE("kill >>> pid : %d, sig : %d", pid, sig);
    //return syscall(__NR_kill, pid, sig);
    //TODO 这里有问题
    JNIEnv *envPtr = ensureEnvCreated();
    if (envPtr == nullptr) {
        return syscall(__NR_kill, pid, sig);
    }

    //public static boolean onKillProcess(int pid, int signal) {
    jmethodID okKillProcessMethod = envPtr->GetStaticMethodID(nativeEngineClass, "onKillProcess",
                                                              "(II)Z");
    jboolean needKill = envPtr->CallStaticBooleanMethod(nativeEngineClass, okKillProcessMethod, pid,
                                                        sig);
    if (needKill) {
        return syscall(__NR_kill, pid, sig);
    }
    return 0;
}

#ifndef __LP64__

// int __statfs64(const char *path, size_t size, struct statfs *stat);
//TODO 文件大小检测
HOOK_DEF(int, __statfs64, const char *pathname, size_t size, struct statfs *stat) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        int ret = syscall(__NR_statfs64, relocated_path, size, stat);
        //DODO
        return ret;
    }
    errno = EACCES;
    return -1;
}

// int __open(const char *pathname, int flags, int mode);
HOOK_DEF(int, __open, const char *pathname, int flags, int mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    ALOGE("__open found %s -----> %s", pathname, relocated_path);
    if (relocated_path && !((flags & O_WRONLY || flags & O_RDWR) && isReadOnly(relocated_path))) {
        int fake_fd = redirect_proc_maps(relocated_path, flags, mode);
        if (fake_fd != 0) {
            return fake_fd;
        }
        return syscall(__NR_open, relocated_path, flags, mode);
    }
    errno = EACCES;
    return -1;
}

// ssize_t readlink(const char *path, char *buf, size_t bufsiz);
HOOK_DEF(ssize_t, readlink, const char *pathname, char *buf, size_t bufsiz) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        char *newBuf = (char *) alloca(bufsiz);
        memset(newBuf, 0, bufsiz);
        long ret = syscall(__NR_readlink, relocated_path, newBuf, bufsiz);
        memcpy(buf, newBuf, bufsiz);
        if (ret < 0) {
            return ret;
        } else {
            // relocate link content
            if (reverse_relocate_path_inplace(buf, bufsiz) != -1) {
                return ret;
            }
        }
    }
    errno = EACCES;
    return -1;
}

// int mkdir(const char *pathname, mode_t mode);
HOOK_DEF(int, mkdir, const char *pathname, mode_t mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_mkdir, relocated_path, mode);
    }
    errno = EACCES;
    return -1;
}

// int rmdir(const char *pathname);
HOOK_DEF(int, rmdir, const char *pathname) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_rmdir, relocated_path);
    }
    errno = EACCES;
    return -1;
}

// int lchown(const char *pathname, uid_t owner, gid_t group);
HOOK_DEF(int, lchown, const char *pathname, uid_t owner, gid_t group) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_lchown, relocated_path, owner, group);
    }
    errno = EACCES;
    return -1;
}

// int utimes(const char *filename, const struct timeval *tvp);
HOOK_DEF(int, utimes, const char *pathname, const struct timeval *tvp) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_utimes, relocated_path, tvp);
    }
    errno = EACCES;
    return -1;
}

// int link(const char *oldpath, const char *newpath);
HOOK_DEF(int, link, const char *oldpath, const char *newpath) {
    char temp[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp, sizeof(temp));
    if (relocated_path_old) {
        return syscall(__NR_link, relocated_path_old, newpath);
    }
    errno = EACCES;
    return -1;
}

// int access(const char *pathname, int mode);
HOOK_DEF(int, access, const char *pathname, int mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (relocated_path && !(mode & W_OK && isReadOnly(relocated_path))) {
        return syscall(__NR_access, relocated_path, mode);
    }
    errno = EACCES;
    return -1;
}

// int chmod(const char *path, mode_t mode);
HOOK_DEF(int, chmod, const char *pathname, mode_t mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_chmod, relocated_path, mode);
    }
    errno = EACCES;
    return -1;
}

// int chown(const char *path, uid_t owner, gid_t group);
HOOK_DEF(int, chown, const char *pathname, uid_t owner, gid_t group) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_chown, relocated_path, owner, group);
    }
    errno = EACCES;
    return -1;
}

// int lstat(const char *path, struct stat *buf);
HOOK_DEF(int, lstat, const char *pathname, struct stat *buf) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_lstat64, relocated_path, buf);
    }
    errno = EACCES;
    return -1;
}

// int stat(const char *path, struct stat *buf);
HOOK_DEF(int, stat, const char *pathname, struct stat *buf) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        long ret = syscall(__NR_stat64, relocated_path, buf);
        if (isReadOnly(relocated_path)) {
            buf->st_mode &= ~S_IWGRP;
        }
        handleMockStat(pathname, buf);
        return ret;
    }
    errno = EACCES;
    return -1;
}

// int symlink(const char *oldpath, const char *newpath);
HOOK_DEF(int, symlink, const char *oldpath, const char *newpath) {
    char temp[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp, sizeof(temp));
    if (relocated_path_old) {
        return syscall(__NR_symlink, relocated_path_old, newpath);
    }
    errno = EACCES;
    return -1;
}

// int unlink(const char *pathname);
HOOK_DEF(int, unlink, const char *pathname) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (relocated_path && !isReadOnly(relocated_path)) {
        return syscall(__NR_unlink, relocated_path);
    }
    errno = EACCES;
    return -1;
}

// int fchmod(const char *pathname, mode_t mode);
HOOK_DEF(int, fchmod, const char *pathname, mode_t mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_fchmod, relocated_path, mode);
    }
    errno = EACCES;
    return -1;
}


// int fstatat(int dirfd, const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstatat, int dirfd, const char *pathname, struct stat *buf, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        int ret =  syscall(__NR_fstatat64, dirfd, relocated_path, buf, flags);
        handleMockStat(pathname,buf);
        return ret;
    }
    errno = EACCES;
    return -1;
}

// int fstat(const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstat, const char *pathname, struct stat *buf) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        int ret =  syscall(__NR_fstat64, relocated_path, buf);
        handleMockStat(pathname,buf);
        return ret;
    }
    errno = EACCES;
    return -1;
}

// int mknod(const char *pathname, mode_t mode, dev_t dev);
HOOK_DEF(int, mknod, const char *pathname, mode_t mode, dev_t dev) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_mknod, relocated_path, mode, dev);
    }
    errno = EACCES;
    return -1;
}

// int rename(const char *oldpath, const char *newpath);
HOOK_DEF(int, rename, const char *oldpath, const char *newpath) {
    char temp_old[PATH_MAX], temp_new[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp_old, sizeof(temp_old));
    const char *relocated_path_new = relocate_path(newpath, temp_new, sizeof(temp_new));
    if (relocated_path_old && relocated_path_new) {
        return syscall(__NR_rename, relocated_path_old, relocated_path_new);
    }
    errno = EACCES;
    return -1;
}

#endif


// int mknodat(int dirfd, const char *pathname, mode_t mode, dev_t dev);
HOOK_DEF(int, mknodat, int dirfd, const char *pathname, mode_t mode, dev_t dev) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_mknodat, dirfd, relocated_path, mode, dev);
    }
    errno = EACCES;
    return -1;
}

// int utimensat(int dirfd, const char *pathname, const struct timespec times[2], int flags);
HOOK_DEF(int, utimensat, int dirfd, const char *pathname, const struct timespec times[2],
         int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_utimensat, dirfd, relocated_path, times, flags);
    }
    errno = EACCES;
    return -1;
}

// int fchownat(int dirfd, const char *pathname, uid_t owner, gid_t group, int flags);
HOOK_DEF(int, fchownat, int dirfd, const char *pathname, uid_t owner, gid_t group, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_fchownat, dirfd, relocated_path, owner, group, flags);
    }
    errno = EACCES;
    return -1;
}

// int chroot(const char *pathname);
HOOK_DEF(int, chroot, const char *pathname) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_chroot, relocated_path);
    }
    errno = EACCES;
    return -1;
}

// int renameat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath);
HOOK_DEF(int, renameat, int olddirfd, const char *oldpath, int newdirfd, const char *newpath) {
    char temp_old[PATH_MAX], temp_new[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp_old, sizeof(temp_old));
    const char *relocated_path_new = relocate_path(newpath, temp_new, sizeof(temp_new));
    if (relocated_path_old && relocated_path_new) {
        return syscall(__NR_renameat, olddirfd, relocated_path_old, newdirfd,
                       relocated_path_new);
    }
    errno = EACCES;
    return -1;
}

// int statfs64(const char *__path, struct statfs64 *__buf) __INTRODUCED_IN(21);
HOOK_DEF(int, statfs64, const char *filename, struct statfs64 *buf) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(filename, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_statfs, relocated_path, buf);
    }
    errno = EACCES;
    return -1;
}

// int unlinkat(int dirfd, const char *pathname, int flags);
HOOK_DEF(int, unlinkat, int dirfd, const char *pathname, int flags) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (relocated_path && !isReadOnly(relocated_path)) {
        return syscall(__NR_unlinkat, dirfd, relocated_path, flags);
    }
    errno = EACCES;
    return -1;
}

// int symlinkat(const char *oldpath, int newdirfd, const char *newpath);
HOOK_DEF(int, symlinkat, const char *oldpath, int newdirfd, const char *newpath) {
    char temp[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp, sizeof(temp));
    if (relocated_path_old) {
        return syscall(__NR_symlinkat, relocated_path_old, newdirfd, newpath);
    }
    errno = EACCES;
    return -1;
}

// int linkat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath, int flags);
HOOK_DEF(int, linkat, int olddirfd, const char *oldpath, int newdirfd, const char *newpath,
         int flags) {
    char temp[PATH_MAX];
    const char *relocated_path_old = relocate_path(oldpath, temp, sizeof(temp));
    if (relocated_path_old) {
        return syscall(__NR_linkat, olddirfd, relocated_path_old, newdirfd, newpath,
                       flags);
    }
    errno = EACCES;
    return -1;
}

// int mkdirat(int dirfd, const char *pathname, mode_t mode);
HOOK_DEF(int, mkdirat, int dirfd, const char *pathname, mode_t mode) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_mkdirat, dirfd, relocated_path, mode);
    }
    errno = EACCES;
    return -1;
}

// int readlinkat(int dirfd, const char *pathname, char *buf, size_t bufsiz);
HOOK_DEF(int, readlinkat, int dirfd, const char *pathname, char *buf, size_t bufsiz) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        // 这里看起来是有内存溢出漏洞，如果使用原生的buf对象。好像使用了溢出空间，会导致返回内容多处一些不该多的东西，具体原因不明。
        // 所以这里新建一片空间用于接受系统调用结果
        char *newBuf = (char *) alloca(bufsiz);
        memset(newBuf, 0, bufsiz);
        long ret = syscall(__NR_readlinkat, dirfd, relocated_path, newBuf, bufsiz);
        memcpy(buf, newBuf, bufsiz);
        if (ret < 0) {
            return ret;
        } else {
            // relocate link content
            if (reverse_relocate_path_inplace(buf, bufsiz) != -1) {
                return ret;
            }
        }
    }
    errno = EACCES;
    return -1;
}


// int truncate(const char *path, off_t length);
HOOK_DEF(int, truncate, const char *pathname, off_t length) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_truncate, relocated_path, length);
    }
    errno = EACCES;
    return -1;
}

// int chdir(const char *path);
HOOK_DEF(int, chdir, const char *pathname) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_chdir, relocated_path);
    }
    errno = EACCES;
    return -1;
}

// int __getcwd(char *buf, size_t size);
HOOK_DEF(int, __getcwd, char *buf, size_t size) {
    long ret = syscall(__NR_getcwd, buf, size);
    if (!ret) {
        if (reverse_relocate_path_inplace(buf, size) < 0) {
            errno = EACCES;
            return -1;
        }
    }
    return ret;
}

// int __openat(int fd, const char *pathname, int flags, int mode);
HOOK_DEF(int, __openat, int fd, const char *pathname, int flags, int mode) {
    char temp[PATH_MAX];

    const char *relocated_path = relocate_path(pathname, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        //now with use MapsHideHandler
        int fake_fd = redirect_proc_maps(relocated_path, flags, mode);
        if (fake_fd != 0) {
            return fake_fd;
        }
        return syscall(__NR_openat, fd, relocated_path, flags, mode);
    }
    errno = EACCES;
    return -1;
}

// int __statfs (__const char *__file, struct statfs *__buf);
// TODO 文件大小检测
HOOK_DEF(int, __statfs, __const char *__file, struct statfs *__buf) {
    char temp[PATH_MAX];
    const char *relocated_path = relocate_path(__file, temp, sizeof(temp));
    if (__predict_true(relocated_path)) {
        return syscall(__NR_statfs, relocated_path, __buf);
    }
    errno = EACCES;
    return -1;
}

static char **relocate_envp(const char *pathname, char *const envp[]) {
    if (strstr(pathname, "libweexjsb.so")) {
        return const_cast<char **>(envp);
    }
    //TODO
    char *soPath = getenv("V_SO_PATH");
    char *soPath64 = getenv("V_SO_PATH_64");

    char *env_so_path = nullptr;
    FILE *fd = fopen(pathname, "r");
    if (!fd) {
        return const_cast<char **>(envp);
    }
    for (int i = 0; i < 4; ++i) {
        fgetc(fd);
    }
    int type = fgetc(fd);
    if (type == ELFCLASS32) {
        env_so_path = soPath;
    } else if (type == ELFCLASS64) {
        env_so_path = soPath64;
    }
    fclose(fd);
    if (env_so_path == nullptr) {
        ALOGW("can not judge bin file type:%s", pathname);
        return const_cast<char **>(envp);
    }
    int len = 0;
    int ld_preload_index = -1;
    int self_so_index = -1;
    while (envp[len]) {
        /* find LD_PRELOAD element */
        if (ld_preload_index == -1 && !strncmp(envp[len], "LD_PRELOAD=", 11)) {
            ld_preload_index = len;
        }
        if (self_so_index == -1 && !strncmp(envp[len], "V_SO_PATH=", 10)) {
            self_so_index = len;
        }
        ++len;
    }
    /* append LD_PRELOAD element */
    if (ld_preload_index == -1) {
        ++len;
    }
    /* append V_env element */
    if (self_so_index == -1) {
        // V_SO_PATH
        // V_API_LEVEL
        // V_PREVIEW_API_LEVEL
        // RATEL_NATIVE_PATH
        // ZE_ORIGIN_PKG
        // ZE_NOW_PKG
        // FP_USER_SEED
        // FP_MAC_FAKE
        len += 8;
        if (soPath64) {
            // V_SO_PATH_64
            len++;
        }
        len += get_keep_item_count();
        len += get_forbidden_item_count();
        len += get_replace_item_count() * 2;
    }

    /* append NULL element */
    ++len;

    char **relocated_envp = (char **) malloc(len * sizeof(char *));
    memset(relocated_envp, 0, len * sizeof(char *));
    for (int i = 0; envp[i]; ++i) {
        if (i != ld_preload_index) {
            relocated_envp[i] = strdup(envp[i]);
        }
    }
    char LD_PRELOAD_VARIABLE[PATH_MAX];
    if (ld_preload_index == -1) {
        ld_preload_index = len - 2;
        sprintf(LD_PRELOAD_VARIABLE, "LD_PRELOAD=%s", env_so_path);
    } else {
        const char *orig_ld_preload = envp[ld_preload_index] + 11;
        sprintf(LD_PRELOAD_VARIABLE, "LD_PRELOAD=%s:%s", env_so_path, orig_ld_preload);
    }
    relocated_envp[ld_preload_index] = strdup(LD_PRELOAD_VARIABLE);
    int index = 0;
    while (relocated_envp[index]) index++;
    if (self_so_index == -1) {
        char element[PATH_MAX] = {0};
        sprintf(element, "V_SO_PATH=%s", soPath);
        relocated_envp[index++] = strdup(element);
        if (soPath64) {
            sprintf(element, "V_SO_PATH_64=%s", soPath64);
            relocated_envp[index++] = strdup(element);
        }
        sprintf(element, "V_API_LEVEL=%s", getenv("V_API_LEVEL"));
        relocated_envp[index++] = strdup(element);
        sprintf(element, "V_PREVIEW_API_LEVEL=%s", getenv("V_PREVIEW_API_LEVEL"));
        relocated_envp[index++] = strdup(element);
        sprintf(element, "RATEL_NATIVE_PATH=%s", getenv("RATEL_NATIVE_PATH"));
        relocated_envp[index++] = strdup(element);
        sprintf(element, "ZE_ORIGIN_PKG=%s", getenv("ZE_ORIGIN_PKG"));
        relocated_envp[index++] = strdup(element);
        sprintf(element, "ZE_NOW_PKG=%s", getenv("ZE_NOW_PKG"));
        relocated_envp[index++] = strdup(element);
        sprintf(element, "FP_USER_SEED=%s", getenv("FP_USER_SEED"));
        relocated_envp[index++] = strdup(element);
        sprintf(element, "FP_MAC_FAKE=%s", getenv("FP_MAC_FAKE"));
        relocated_envp[index++] = strdup(element);


        for (int i = 0; i < get_keep_item_count(); ++i) {
            PathItem &item = get_keep_items()[i];
            char env[PATH_MAX] = {0};
            sprintf(env, "V_KEEP_ITEM_%d=%s", i, item.path);
            relocated_envp[index++] = strdup(env);
        }

        for (int i = 0; i < get_forbidden_item_count(); ++i) {
            PathItem &item = get_forbidden_items()[i];
            char env[PATH_MAX] = {0};
            sprintf(env, "V_FORBID_ITEM_%d=%s", i, item.path);
            relocated_envp[index++] = strdup(env);
        }

        for (int i = 0; i < get_replace_item_count(); ++i) {
            ReplaceItem &item = get_replace_items()[i];
            char src[PATH_MAX] = {0};
            char dst[PATH_MAX] = {0};
            sprintf(src, "V_REPLACE_ITEM_SRC_%d=%s", i, item.orig_path);
            sprintf(dst, "V_REPLACE_ITEM_DST_%d=%s", i, item.new_path);
            relocated_envp[index++] = strdup(src);
            relocated_envp[index++] = strdup(dst);
        }
    }
//    for (int j = 0; j < index; j++) {
//        ALOGI("relocated_envp index:%d value:%s", j, relocated_envp[j]);
//    }
    return relocated_envp;
}


// int (*origin_execve)(const char *pathname, char *const argv[], char *const envp[]);
HOOK_DEF(int, execve, const char *pathname, char *argv[], char *const envp[]) {
    return handle_execve(pathname, argv, envp);
}

HOOK_DEF(void *, dlopen_CI, const char *filename, int flag) {
    char temp[PATH_MAX];
    const char *redirect_path = relocate_path(filename, temp, sizeof(temp));
    void *ret = orig_dlopen_CI(redirect_path, flag);
    onSoLoaded(filename, ret);
    return ret;
}

HOOK_DEF(void*, do_dlopen_CIV, const char *filename, int flag, const void *extinfo) {
    char temp[PATH_MAX];
    const char *redirect_path = relocate_path(filename, temp, sizeof(temp));
    void *ret = orig_do_dlopen_CIV(redirect_path, flag, extinfo);
    onSoLoaded(filename, ret);
    return ret;
}

HOOK_DEF(void*, do_dlopen_CIVV, const char *name, int flags, const void *extinfo,
         void *caller_addr) {
    char temp[PATH_MAX];
    const char *redirect_path = relocate_path(name, temp, sizeof(temp));
    void *ret = orig_do_dlopen_CIVV(redirect_path, flags, extinfo, caller_addr);
    onSoLoaded(name, ret);
    return ret;
}

//void *dlsym(void *handle, const char *symbol)
HOOK_DEF(void*, dlsym, void *handle, char *symbol) {
    return orig_dlsym(handle, symbol);
}

HOOK_DEF(pid_t, vfork) {
    return fork();
}

__END_DECLS
// end IO DEF


bool relocate_linker(const char *linker_path) {
    intptr_t linker_addr, dlopen_off, symbol;
    if ((linker_addr = get_addr(linker_path)) == 0) {
        ALOGE("Cannot found linker addr: %s", linker_path);
        return false;
    }
    if (resolve_symbol(linker_path, "__dl__Z9do_dlopenPKciPK17android_dlextinfoPKv",
                       &dlopen_off) == 0) {
        ALOGE("resolve linker symobl __dl__Z9do_dlopenPKciPK17android_dlextinfoPKv addr: %s",
              linker_path);
        symbol = linker_addr + dlopen_off;
        MSHookFunctionOld((void *) symbol, (void *) new_do_dlopen_CIVV,
                          (void **) &orig_do_dlopen_CIVV);
        return true;
    } else if (resolve_symbol(linker_path, "__dl__Z9do_dlopenPKciPK17android_dlextinfoPv",
                              &dlopen_off) == 0) {
        ALOGE("resolve linker symobl __dl__Z9do_dlopenPKciPK17android_dlextinfoPv addr: %s",
              linker_path);
        symbol = linker_addr + dlopen_off;
        MSHookFunctionOld((void *) symbol, (void *) new_do_dlopen_CIVV,
                          (void **) &orig_do_dlopen_CIVV);
        return true;
    } else if (resolve_symbol(linker_path, "__dl__ZL10dlopen_extPKciPK17android_dlextinfoPv",
                              &dlopen_off) == 0) {
        ALOGE("resolve linker symobl __dl__ZL10dlopen_extPKciPK17android_dlextinfoPv addr: %s",
              linker_path);
        symbol = linker_addr + dlopen_off;
        MSHookFunctionOld((void *) symbol, (void *) new_do_dlopen_CIVV,
                          (void **) &orig_do_dlopen_CIVV);
        return true;
    } else if (
            resolve_symbol(linker_path, "__dl__Z20__android_dlopen_extPKciPK17android_dlextinfoPKv",
                           &dlopen_off) == 0) {
        ALOGE("resolve linker symobl __dl__Z20__android_dlopen_extPKciPK17android_dlextinfoPKv addr: %s",
              linker_path);
        symbol = linker_addr + dlopen_off;
        MSHookFunctionOld((void *) symbol, (void *) new_do_dlopen_CIVV,
                          (void **) &orig_do_dlopen_CIVV);
        return true;
    } else if (
            resolve_symbol(linker_path, "__dl___loader_android_dlopen_ext",
                           &dlopen_off) == 0) {
        ALOGE("resolve linker symobl __dl___loader_android_dlopen_ext addr: %s", linker_path);
        symbol = linker_addr + dlopen_off;
        MSHookFunctionOld((void *) symbol, (void *) new_do_dlopen_CIVV,
                          (void **) &orig_do_dlopen_CIVV);
        return true;
    } else if (resolve_symbol(linker_path, "__dl__Z9do_dlopenPKciPK17android_dlextinfo",
                              &dlopen_off) == 0) {
        ALOGE("resolve linker symobl __dl__Z9do_dlopenPKciPK17android_dlextinfo addr: %s",
              linker_path);
        symbol = linker_addr + dlopen_off;
        MSHookFunctionOld((void *) symbol, (void *) new_do_dlopen_CIV,
                          (void **) &orig_do_dlopen_CIV);
        return true;
    } else if (resolve_symbol(linker_path, "__dl__Z8__dlopenPKciPKv",
                              &dlopen_off) == 0) {
        ALOGE("resolve linker symobl __dl__Z8__dlopenPKciPKv addr: %s", linker_path);
        symbol = linker_addr + dlopen_off;
        MSHookFunctionOld((void *) symbol, (void *) new_do_dlopen_CIV,
                          (void **) &orig_do_dlopen_CIV);
        return true;
    } else if (resolve_symbol(linker_path, "__dl___loader_dlopen",
                              &dlopen_off) == 0) {
        ALOGE("resolve linker symobl __dl___loader_dlopen addr: %s", linker_path);
        symbol = linker_addr + dlopen_off;
        MSHookFunctionOld((void *) symbol, (void *) new_do_dlopen_CIV,
                          (void **) &orig_do_dlopen_CIV);
        return true;
    } else if (resolve_symbol(linker_path, "__dl_dlopen",
                              &dlopen_off) == 0) {
        ALOGE("resolve linker symobl __dl_dlopen addr: %s", linker_path);
        symbol = linker_addr + dlopen_off;
        MSHookFunctionOld((void *) symbol, (void *) new_dlopen_CI,
                          (void **) &orig_dlopen_CI);
        return true;
    }
    ALOGE("do not resolve linker symobl dlopen: %s", linker_path);
    return false;
}

#if defined(__aarch64__)

bool on_found_syscall_aarch64(const char *path, int num, void *func) {
    static int pass = 0;
    switch (num) {
        HOOK_SYSCALL(fchmodat)
        HOOK_SYSCALL(fchownat)
        HOOK_SYSCALL(renameat)
        HOOK_SYSCALL(mkdirat)
        HOOK_SYSCALL(mknodat)
        HOOK_SYSCALL(truncate)
        HOOK_SYSCALL(linkat)
        HOOK_SYSCALL(faccessat)
        HOOK_SYSCALL_(statfs)
        HOOK_SYSCALL_(getcwd)
        HOOK_SYSCALL_(openat)
        HOOK_SYSCALL(readlinkat)
        HOOK_SYSCALL(unlinkat)
        HOOK_SYSCALL(symlinkat)
        HOOK_SYSCALL(utimensat)
        HOOK_SYSCALL(chdir)
        HOOK_SYSCALL(execve)
        HOOK_SYSCALL(kill)
    }
    if (pass == 18) {
        return BREAK_FIND_SYSCALL;
    }
    return CONTINUE_FIND_SYSCALL;
}

bool on_found_linker_syscall_arch64(const char *path, int num, void *func) {
    switch (num) {
        case __NR_openat:
            MSHookFunction(func, (void *) new___openat, (void **) &orig___openat);
            return BREAK_FIND_SYSCALL;
    }
    return CONTINUE_FIND_SYSCALL;
}

#else

bool on_found_linker_syscall_arm(const char *path, int num, void *func) {
    switch (num) {
        case __NR_openat:
            MSHookFunction(func, (void *) new___openat, (void **) &orig___openat);
            break;
        case __NR_open:
            MSHookFunction(func, (void *) new___open, (void **) &orig___open);
            break;
    }
    return CONTINUE_FIND_SYSCALL;
}

#endif


void onSoLoaded(const char *name, void *handle) {
    if (name != nullptr) {
        ALOGE("dlopen found so name: %s", name);
        if (strstr(name, "/data/app/") || strstr(name, "/data/user/")) {
#if defined(__arm64__) || defined(__aarch64__)
            findSyscalls(name, on_found_syscall_aarch64);
#elif defined(__arm__)
            findSyscalls(name, on_found_linker_syscall_arm);
#else
#error "Not Support Architecture."
#endif
            ALOGE("hook svc from so name: %s, over", name);
        }
    }


}

int (*old__system_property_get)(const char *__name, char *__value);

//我们之前从native反射到java，有很大问题。华为有一个机型直接卡白屏。目前原因未知，我们将属性替换重心逻辑由java层迁移到native层
//这样会损失一些灵活性，因为不能回调java的handler了。所有替换项需要提前设置
int new__system_property_get(const char *__name, char *__value) {
    int result = old__system_property_get(__name, __value);
    if (result < 0) {
        //failed
        return result;
    }

    PropertiesMockItem *propertiesMockItem = query_mock_properties(__name);
    if (propertiesMockItem == nullptr) {
        return result;
    }

    if (propertiesMockItem->properties_value == nullptr) {
        result = -1;
        __value[0] = '\0';
        return result;
    }

    result = propertiesMockItem->value_length;
    strcpy(__value, propertiesMockItem->properties_value);
    return result;
}


int (*old_SystemPropertiesGet)(void *hidden_this, const char *__name, char *__value);

int new_SystemPropertiesGet(void *hidden_this, const char *__name, char *__value) {
    int result = old_SystemPropertiesGet(hidden_this, __name, __value);
    // ALOGI("get property name=%s,value=%s", __name, __value);
    // replace_property_if_need(__name, __value);
    return result;
}

void bhook_callback(
        bytehook_stub_t task_stub, //存根，全局唯一，用于unhook
        int status_code, //hook执行状态码
        const char *caller_path_name, //调用者的pathname或basename
        const char *sym_name, //函数名
        void *new_func, //新函数地址
        void *prev_func, //原函数地址
        void *arg) {
    ALOGI("bhook hook %s result: %d", sym_name, status_code);
}

int new_bhook_system_property_get(const char *__name, char *__value) {
    // 每个 proxy 函数中都必须执行 ByteHook 的 stack 清理逻辑
    BYTEHOOK_STACK_SCOPE();
    ALOGD("__system_property_get: %s", __name);
    int result = BYTEHOOK_CALL_PREV(new_bhook_system_property_get, __name, __value);
    if (result < 0) {
        //failed
        return result;
    }

    PropertiesMockItem *propertiesMockItem = query_mock_properties(__name);
    if (propertiesMockItem == nullptr) {
        return result;
    }

    if (propertiesMockItem->properties_value == nullptr) {
        result = -1;
        __value[0] = '\0';
        return result;
    }

    result = propertiesMockItem->value_length;
    ALOGD("__system_property_get: %s , origin value:%s,replaced value:%s", __name, __value,
          propertiesMockItem->properties_value);
    strcpy(__value, propertiesMockItem->properties_value);
    return result;
}

class IORelocatExecveHandler : public ExecveChainHandler {
protected:
    bool beforeExeceve(ExecveParam *execveParam) override {
        char temp[PATH_MAX];
        const char *relocated_path = relocate_path(execveParam->pathname, temp, sizeof(temp));
        if (!relocated_path) {
            errno = EACCES;
            execveParam->ret = -1;
            execveParam->returnEarly = true;
            return false;
        }
        char **relocated_envp = relocate_envp(relocated_path, execveParam->envp);
        execveParam->envp = relocated_envp;
        return true;
    }

    void afterExeceve(ExecveParam originExecveParam, ExecveParam afterExecveParam) override {
        if (originExecveParam.envp == afterExecveParam.envp) {
            return;
        }
        char *const *relocated_envp = afterExecveParam.envp;
        int i = 0;
        while (relocated_envp[i] != nullptr) {
            free(relocated_envp[i]);
            ++i;
        }
        free((void *) relocated_envp);
    }
};

const char *libc_path;
const char *linker_path;

//TODO 暂时不在exec中hook properties
void hookProperties(int api_level, void *handle) {
    if (api_level < 28) {
        hook_function(handle, "__system_property_get", (void *) &new__system_property_get,
                      (void **) &old__system_property_get);
    } else {
        // android 9 __system_property_get函数太短，不能使用inline hook，这里暂时使用PLT 替代
        // android 9上面的__system_property_get是一个跳转函数，内部调用 __fastcall SystemProperties::Get(SystemProperties *__hidden this, const char *, char *)。
        // 但是这个函数对应的符号：_ZN16SystemProperties3GetEPKcPc没有导出，也无法通过 dlfcn_nougat获得
        int register_ret = xhook_register(libc_path, "__system_property_get",
                                          (void *) &new__system_property_get,
                                          (void **) &old__system_property_get);
        if (register_ret != 0) {
            ALOGE("register __system_property_get PLT hook failed :%d", register_ret);
        }
        int xook_refresh_ret = xhook_refresh(1);
        if (xook_refresh_ret != 0) {
            ALOGE("register xook_refresh failed :%d", xook_refresh_ret);
        }
    }
}


FILE *(*origin_popen)(const char *command, const char *type);

#define _LINE_LENGTH 300

FILE *new_popen(const char *command, const char *type) {
    VLOGI("open: %s", command);
    FILE *fp = origin_popen(command, type);
    if (fp == nullptr) {
        VLOGI("execute command failed");
        return fp;
    }


    char line[_LINE_LENGTH];
    while (fgets(line, _LINE_LENGTH, fp) != nullptr) {
        VLOGI("out: %s\n", line);
    }

    pclose(fp);

    return origin_popen(command, type);
}

//property name=gsm.version.baseband,value=MPSS-SDM710-0727_1333_2b0e169
void startIOHook(int api_level, bool needHookProperties) {
    ALOGE("hook start : startIOHook");

    if (api_level >= ANDROID_Q) {
        if (BYTE_POINT == 8) {
            libc_path = "/apex/com.android.runtime/lib64/bionic/libc.so";
            linker_path = "/apex/com.android.runtime/bin/linker64";
        } else {
            libc_path = "/apex/com.android.runtime/lib/bionic/libc.so";
            linker_path = "/apex/com.android.runtime/bin/linker";
        }
    } else {
        if (BYTE_POINT == 8) {
            libc_path = "/system/lib64/libc.so";
            linker_path = "/system/bin/linker64";
        } else {
            libc_path = "/system/lib/libc.so";
            linker_path = "/system/bin/linker";
        }
    }

    void *handle = dlopen("libc.so", RTLD_NOW);
    if (handle) {
        if (needHookProperties) {
            hookProperties(api_level, handle);
        }

        hook_function(handle, "getifaddrs", (void *) &new_getifaddrs,
                      (void **) &origin_getifaddrs);
        // 避免通过命令行调用ip address，或者ip link获取真实mac地址
        hook_libipcommand();

        hook_function_old(handle, "popen", (void *) &new_popen,
                          (void **) &origin_popen);

#ifdef RATEL_BUILD_TYPE_DEBUG
        // print native command
        register_execve_handler(new ExecveTraceHandler());
#endif
        // register_execve_handler(new ExecveTraceHandler());
        register_execve_handler(new IORelocatExecveHandler());

#if defined(__aarch64__)
        HOOK_SYMBOL(handle, fchownat);
        HOOK_SYMBOL(handle, renameat);
        HOOK_SYMBOL(handle, mkdirat);
        HOOK_SYMBOL(handle, mknodat);
        HOOK_SYMBOL(handle, truncate);
        HOOK_SYMBOL(handle, linkat);
        HOOK_SYMBOL(handle, readlinkat);
        HOOK_SYMBOL(handle, unlinkat);
        HOOK_SYMBOL(handle, symlinkat);
        HOOK_SYMBOL(handle, utimensat);
        HOOK_SYMBOL(handle, chdir);
        HOOK_SYMBOL(handle, execve);
        HOOK_SYMBOL(handle, statfs64);
        HOOK_SYMBOL(handle, kill);
        HOOK_SYMBOL(handle, vfork);
        HOOK_SYMBOL(handle, fstatat64);
        findSyscalls(libc_path, on_found_syscall_aarch64);
        findSyscalls(linker_path, on_found_linker_syscall_arch64);

        bool linker_result = relocate_linker(linker_path);
        ALOGE("hook 64 linker_result : %d", linker_result);
#else
        HOOK_SYMBOL(handle, faccessat);
        HOOK_SYMBOL(handle, __openat);
        HOOK_SYMBOL(handle, fchmodat);
        HOOK_SYMBOL(handle, fchownat);
        HOOK_SYMBOL(handle, renameat);
        HOOK_SYMBOL(handle, fstatat64);
        HOOK_SYMBOL(handle, __statfs);
        HOOK_SYMBOL(handle, __statfs64);
        HOOK_SYMBOL(handle, mkdirat);
        HOOK_SYMBOL(handle, mknodat);
        HOOK_SYMBOL(handle, truncate);
        HOOK_SYMBOL(handle, linkat);
        HOOK_SYMBOL(handle, readlinkat);
        HOOK_SYMBOL(handle, unlinkat);
        HOOK_SYMBOL(handle, symlinkat);
        HOOK_SYMBOL(handle, utimensat);
        HOOK_SYMBOL(handle, __getcwd);
        HOOK_SYMBOL(handle, chdir);
        HOOK_SYMBOL(handle, execve);
        HOOK_SYMBOL(handle, kill);
        HOOK_SYMBOL(handle, vfork);
        if (api_level <= 20) {
            HOOK_SYMBOL(handle, access);
            HOOK_SYMBOL(handle, stat);
            HOOK_SYMBOL(handle, lstat);
            HOOK_SYMBOL(handle, fstatat);
            HOOK_SYMBOL(handle, __open);
            HOOK_SYMBOL(handle, chmod);
            HOOK_SYMBOL(handle, chown);
            HOOK_SYMBOL(handle, rename);
            HOOK_SYMBOL(handle, rmdir);
            HOOK_SYMBOL(handle, mkdir);
            HOOK_SYMBOL(handle, mknod);
            HOOK_SYMBOL(handle, link);
            HOOK_SYMBOL(handle, unlink);
            HOOK_SYMBOL(handle, readlink);
            HOOK_SYMBOL(handle, symlink);
        }
#ifdef __arm__
        bool linker_result = relocate_linker(linker_path);
        ALOGE("hook 32 linker_result : %d", linker_result);
        if (!linker_result) {
            findSyscalls(linker_path, on_found_linker_syscall_arm);
        }
#endif

#endif
        dlclose(handle);
    }
}


void
IOUniformer::startUniformer(int api_level,
                            int preview_api_level, const char *so_path, const char *so_path_64) {
    char api_level_chars[56];
    setenv("V_SO_PATH", so_path, 1);
    setenv("V_SO_PATH_64", so_path_64, 1);
    sprintf(api_level_chars, "%i", api_level);
    setenv("V_API_LEVEL", api_level_chars, 1);
    sprintf(api_level_chars, "%i", preview_api_level);
    setenv("V_PREVIEW_API_LEVEL", api_level_chars, 1);
    setenv("FP_MAC_FAKE", open_mac_address_fake ? "1" : "0", 1);
    startIOHook(api_level, true);
    IS_EXECVE_SUB_PROCESS = false;
}
