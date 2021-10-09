#include <cstring>
#include <cstdio>
#include <climits>
#include <unistd.h>
#include <cstdlib>
#include <syscall.h>
#include <cerrno>
#include <fcntl.h>
#include <sys/stat.h>
#include "MapsRedirector.h"
#include "Log.h"
#include "SandboxFs.h"
#include "Helper.h"

extern int64_t mock_memory_diff;


typedef unsigned long addr_t;

static int create_temp_file() {
    // TODO: O_RDONLY FAKE_PATH
    char pattern[PATH_MAX] = {0};
    char *cache_dir = getenv("RATEL_NATIVE_PATH");
    int fd = open(pattern, O_RDWR | O_CLOEXEC | O_TMPFILE | O_EXCL, NULL);
    if (fd != -1) {
        return fd;
    }

    snprintf(pattern, sizeof(pattern), "%s/dev_maps_%d_%d", cache_dir, getpid(), gettid());
    //ALOGI("create a temp maps temp file:%s", pattern);
    fd = open(pattern, O_CREAT | O_RDWR | O_TRUNC | O_CLOEXEC, NULL);
    if (fd == -1) {
        ALOGE("fake_maps: cannot create tmp file, errno = %d", errno);
        return -1;
    }
    unlink(pattern);
    return fd;
}

static char *match_maps_item(char *line) {
    char *p = strstr(line, " /data/");
    return p;
}


static bool filter_for_ratel(const char *mapsLine) {
    if (strstr(mapsLine, "libratelnative")) {
        return true;
    }
    if (strstr(mapsLine, "libsandhook")) {
        return true;
    }
    if (strstr(mapsLine, "ratel_container_xposed_module")) {
        return true;
    }
    if (strstr(mapsLine, "ndHookerNew_opt")) {
        return true;
    }
//    if (strstr(mapsLine, "ratel_container_origin_apk")) {
//        return true;
//    }
    return strstr(mapsLine, "ratel_container-driver") != nullptr;
}


static void redirect_proc_maps_internal(const int fd, const int fake_fd) {
    char line[PATH_MAX];
    char *p = line, *e;
    size_t n = PATH_MAX - 1;
    ssize_t r;
    while ((r = TEMP_FAILURE_RETRY(read(fd, p, n))) > 0) {
        p[r] = '\0';
        p = line; // search begin at line start

        while ((e = strchr(p, '\n')) != nullptr) {
            e[0] = '\0';

            // 指向 /data/
            // 如：  /data/app/com.rytong.airchina-2/oat/arm/base.odex
            //      /data/app/com.rytong.airchina-2/base.apk
            //ALOGI("maps: %s", p);
            char *path = match_maps_item(p);
            if (path == nullptr) {
                // 非/data 目录的，不进行重定向
                e[0] = '\n';
                write(fake_fd, p, e - p + 1);
                p = e + 1;
                continue;
            }
            ++path; // skip blank 嗯，第一个字符串是空格，需要skip
            if (filter_for_ratel(path)) {
                //ratel 框架相关的，隐藏掉
                // ALOGE("remove map item for ratel: %s", p);
                p = e + 1;
                continue;
            }

            if (odex_file_path != nullptr) {
                if (end_with(p, "/base.odex") && strstr(p, "/data/app/")) {
                    //这种情况，证明需要隐藏odex
                    p = e + 1;
                    continue;
                }
                if (strstr(p, "ratel_container_origin_apk")) {
                    // 把这个伪装为 base.odex
                    write(fake_fd, p, path - p);
                    write(fake_fd, odex_file_path, strlen(odex_file_path));
                    write(fake_fd, "\n", 1);
                    p = e + 1;
                    continue;
                }
            }

            e[0] = '\n';
            write(fake_fd, p, e - p + 1);
            p = e + 1;
        }
        if (p == line) { // !any_entry
            ALOGE("fake_maps: cannot process line larger than %u bytes!", PATH_MAX);
            goto __break;
        } //if

        const size_t remain = strlen(p);
        if (remain <= (PATH_MAX / 2)) {
            memcpy(line, p, remain * sizeof(p[0]));
        } else {
            memmove(line, p, remain * sizeof(p[0]));
        } //if

        p = line + remain;
        n = PATH_MAX - 1 - remain;
    }

    __break:
    return;
}


int handle_redirect_proc_maps(const char *s, const char *const pathname, const int flags,
                              const int mode) {
    // '/maps'
    const char *p = strstr(s, "/maps");
    if (p == nullptr || *(p + sizeof("/maps") - 1) != '\0') {
        p = strstr(s, "/smaps");
        if (p == nullptr || *(p + sizeof("/smaps") - 1) != '\0') {
            return 0;
        }
    }
    ALOGE("start redirect: %s", pathname);

    int fd = syscall(__NR_openat, AT_FDCWD, pathname, flags, mode);
    if (fd == -1) {
        errno = EACCES;
        return -1;
    }

    int fake_fd = create_temp_file();
    if (fake_fd == -1) {
        ALOGE("fake_maps: create_temp_file failed, errno = %d", errno);
        errno = EACCES;
        return -1;
    }

    redirect_proc_maps_internal(fd, fake_fd);
    lseek(fake_fd, 0, SEEK_SET);
    syscall(__NR_close, fd);

    ALOGI("fake_maps: faked %s -> fd %d", pathname, fake_fd);
    return fake_fd;
}

static void redirect_proc_status_internal(const int fd, const int fake_fd) {
    char buffer[PATH_MAX];
    char *line_iterator = buffer;
    size_t max_read_count = PATH_MAX - 1;
    ssize_t read_size;
    while ((read_size = TEMP_FAILURE_RETRY(read(fd, line_iterator, max_read_count))) > 0) {
        //读取到的字符串，后面补零。
        line_iterator[read_size] = '\0';
        line_iterator = buffer; // search begin at line start

        char *line_end;
        while ((line_end = strchr(line_iterator, '\n')) != nullptr) {
            // \n替换层\0之后，line_iterator 就变成单独的一行了
            line_end[0] = '\0';

            //ALOGE("proc_status line: %s", line_iterator);
            //TracerPid:	0
            //Uid:	2000	2000	2000	2000
            // 判断是否是TracerPid
            if (!start_with(line_iterator, "TracerPid:")) {
                line_end[0] = '\n';
                write(fake_fd, line_iterator, line_end - line_iterator + 1);
                line_iterator = line_end + 1;
                continue;
            }

            write(fake_fd, "TracerPid:\t0\n", strlen("TracerPid:\t0\n"));

            line_end[0] = '\n';
            line_iterator = line_end + 1;
        }
        if (line_iterator == buffer) { // !any_entry
            ALOGE("fake_maps: cannot process line larger than %u bytes!", PATH_MAX);
            goto __break;
        } //if

        const size_t remain = strlen(line_iterator);
        if (remain <= (PATH_MAX / 2)) {
            memcpy(buffer, line_iterator, remain * sizeof(line_iterator[0]));
        } else {
            memmove(buffer, line_iterator, remain * sizeof(line_iterator[0]));
        } //if

        line_iterator = buffer + remain;
        max_read_count = PATH_MAX - 1 - remain;
    }

    __break:
    return;
}

int handle_redirect_meminfo_maps(const char *s, const char *const pathname, const int flags,
                                 const int mode) {
    // '/maps'
    const char *p = strstr(s, "/status");
    if (p == nullptr) {
        return 0;
    }
    ALOGE("start redirect: %s", pathname);

    int fd = syscall(__NR_openat, AT_FDCWD, pathname, flags, mode);
    if (fd == -1) {
        errno = EACCES;
        return -1;
    }

    int fake_fd = create_temp_file();
    if (fake_fd == -1) {
        ALOGE("fake_maps: create_temp_file failed, errno = %d", errno);
        errno = EACCES;
        return -1;
    }

    redirect_proc_status_internal(fd, fake_fd);
    lseek(fake_fd, 0, SEEK_SET);
    syscall(__NR_close, fd);

    ALOGI("fake_maps: faked %s -> fd %d", pathname, fake_fd);
    return fake_fd;
}

int redirect_proc_maps(const char *const pathname, const int flags, const int mode) {
    // '/proc/self/maps'
    if (strncmp(pathname, "/proc/", sizeof("/proc/") - 1) != 0) {
        return 0;
    }
    //ALOGI("handle redirect_proc");

    // 'self/maps'
    const char *s = pathname + sizeof("/proc/") - 1;

//    int ret_fd = handle_redirect_proc_maps(s, pathname, flags, mode);
//    if (ret_fd != 0) {
//        return ret_fd;
//    }

// 滴滴加油，这里会导致weex框架加载失败，具体原因待分析，暂时禁用这个功能
//#ifdef RATEL_BUILD_TYPE_DEBUG
//    return handle_redirect_meminfo_maps(s, pathname, flags, mode);
//#else
//    ALOGI("this is release build");
//    return 0;
//#endif
    return 0;
}


