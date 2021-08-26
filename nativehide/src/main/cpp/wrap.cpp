#include <cstdint>
#include <sys/mount.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <cstring>
#include <dirent.h>
#include <unistd.h>
#include <sched.h>
#include <fcntl.h>
#include <cstdio>
#include <cerrno>


DIR *_opendir(const char *path) {
    DIR *d = opendir(path);
    if (d == nullptr) {
        printf("_opendir fail");
    }
    return d;
}

struct dirent *_readdir(DIR *dir) {
    errno = 0;
    struct dirent *ent = readdir(dir);
    if (errno && ent == nullptr) {
        printf("_readdir fail");
    }
    return ent;
}

int _mprotect(void *addr, size_t size, int prot) {
    int res = mprotect(addr, size, prot);
    if (res != 0) {
        printf("mprotect fail");
    }
    return res;
}

void *_mmap(void *addr, size_t size, int prot, int flags, int fd, off_t offset) {
    auto res = mmap(addr, size, prot, flags, fd, offset);
    if (res == MAP_FAILED) {
        printf("_mmap fail");
    }
    return res;
}