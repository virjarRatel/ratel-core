#include <cinttypes>
#include <sys/mman.h>
#include <Log.h>
#include "pmparser.h"
#include "wrap.h"

#include "hide.h"

/**
 * https://github.com/RikkaApps/Riru/blob/master/core/src/main/cpp/hide.cpp
 */

/**
 * Magic to hide from /proc/###/maps, the idea is from Haruue Icymoon (https://github.com/haruue)
 */
//#ifdef __LP64__
//#define LIB_PATH "/system/lib64/"
//#else
//#define LIB_PATH "/system/lib/"
//#endif
#define LIB_PATH ""
struct hide_struct {
    procmaps_struct *original;
    uintptr_t backup_address;
};

static int get_prot(const procmaps_struct *procstruct) {
    int prot = 0;
    if (procstruct->is_r) {
        prot |= PROT_READ;
    }
    if (procstruct->is_w) {
        prot |= PROT_WRITE;
    }
    if (procstruct->is_x) {
        prot |= PROT_EXEC;
    }
    return prot;
}

static int do_hide(hide_struct *data) {
    auto procstruct = data->original;
    auto start = (uintptr_t) procstruct->addr_start;
    auto end = (uintptr_t) procstruct->addr_end;
    auto length = end - start;
    int prot = get_prot(procstruct);

    // backup
    data->backup_address = (uintptr_t) _mmap(nullptr, length, PROT_READ | PROT_WRITE,
                                             MAP_ANONYMOUS | MAP_PRIVATE, -1, 0);
    if (data->backup_address == (uintptr_t) MAP_FAILED) {
        return 1;
    }
    MLOGD("%" PRIxPTR"-%" PRIxPTR" %s %ld %s is backup to %" PRIxPTR, start, end, procstruct->perm,
          procstruct->offset, procstruct->pathname,
          data->backup_address);

    if (!procstruct->is_r) {
        MLOGD("mprotect +r");
        _mprotect((void *) start, length, prot | PROT_READ);
    }
    MLOGD("memcpy -> backup");
    memcpy((void *) data->backup_address, (void *) start, length);

    // munmap original
    MLOGD("munmap original");
    munmap((void *) start, length);

    // restore
    MLOGD("mmap original");
    _mmap((void *) start, length, prot, MAP_ANONYMOUS | MAP_PRIVATE, -1, 0);
    MLOGD("mprotect +w");
    _mprotect((void *) start, length, prot | PROT_WRITE);
    MLOGD("memcpy -> original");
    memcpy((void *) start, (void *) data->backup_address, length);
    if (!procstruct->is_w) {
        MLOGD("mprotect -w");
        _mprotect((void *) start, length, prot);
    }
    return 0;
}

bool start_with_1(const char *input, const char *preffix) {
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


static bool is_data(char *line) {
    return start_with_1(line, "/data/");
}


static bool filter_for_ratel(const char *mapsLine) {
    if (strstr(mapsLine, "ratelhide")) {
        return true;
    }
    if (strstr(mapsLine, "libsandhook")) {
        return true;
    }
    if (strstr(mapsLine, "ratel_container_xposed_module")) {
        return true;
    }
    if (strstr(mapsLine, "SandHookerNew")) {
        return true;
    }
    if (strstr(mapsLine, "ratel_container_origin_apk")) {
        return true;
    }
    if (start_with_1(mapsLine, "/data/app/ratel.")) {
        //maybe ratel module
        return true;
    }
    return strstr(mapsLine, "ratel_container-driver") != nullptr;
}

int riru_hide(bool isMainLib) {
    MLOGI("call riru_hide");
    procmaps_iterator *maps = pmparser_parse(getpid());
    if (maps == nullptr) {
        MLOGE("cannot parse the memory map");
        return false;
    }

//    char buf[PATH_MAX];
    hide_struct *data = nullptr;
    size_t data_count = 0;
    procmaps_struct *maps_tmp;
    while ((maps_tmp = pmparser_next(maps)) != nullptr) {
        if (isMainLib) {
            // 主lib中，隐藏除开自己的其他so文件
            if (!start_with_1(maps_tmp->pathname, "/data/")) {
                continue;
            }
            if (!filter_for_ratel(maps_tmp->pathname)) {
                continue;
            }
        } else {
            // 非主lib中，隐藏 libratelnative
            if (!strstr(maps_tmp->pathname, "libratelnative")) {
                continue;
            }
        }

        MLOGI("find maps relate feature filed:%s", maps_tmp->pathname);

        auto start = (uintptr_t) maps_tmp->addr_start;
        auto end = (uintptr_t) maps_tmp->addr_end;
        if (maps_tmp->is_r) {
            if (data) {
                data = (hide_struct *) realloc(data, sizeof(hide_struct) * (data_count + 1));
            } else {
                data = (hide_struct *) malloc(sizeof(hide_struct));
            }
            data[data_count].original = maps_tmp;
            data_count += 1;
        }
        MLOGD("%" PRIxPTR"-%" PRIxPTR" %s %ld %s", start, end, maps_tmp->perm, maps_tmp->offset,
              maps_tmp->pathname);
    }

    for (int i = 0; i < data_count; ++i) {
        do_hide(&data[i]);
    }

    if (data) free(data);
    pmparser_free(maps);
    return 0;
}