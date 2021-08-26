#include <cstdlib>
#include <cstring>
#include <VAJni.h>
#include "SandboxFs.h"
#include "canonicalize_md.h"
#include "Log.h"
#include "NativeStackTracer.h"

PathItem *keep_items;
PathItem *forbidden_items;
PathItem *readonly_items;
ReplaceItem *replace_items;
StatMockItem *stat_mock_items;

int keep_item_count;
int forbidden_item_count;
int readonly_item_count;
int replace_item_count;
int stat_mock_count;

int add_stat_mock_item(const char *path, int64_t last_modified) {
    char stat_mock_env_name[KEY_MAX];
    sprintf(stat_mock_env_name, "V_STAT_MOCK_ITEM_%d", stat_mock_count);
    setenv(stat_mock_env_name, path, 1);

    char stat_mock_env_value[KEY_MAX];
    //l = last m = modified
    sprintf(stat_mock_env_value, "V_STAT_L_M_%d", stat_mock_count);
    char last_modified_buff[KEY_MAX];
    sprintf(last_modified_buff, "%lld", last_modified);
    setenv(stat_mock_env_value, last_modified_buff, 1);

    stat_mock_items = (StatMockItem *) realloc(stat_mock_items,
                                               stat_mock_count * sizeof(StatMockItem) +
                                               sizeof(StatMockItem));
    StatMockItem &item = stat_mock_items[stat_mock_count];
    item.path = strdup(path);
    item.last_modified = last_modified;
    return ++stat_mock_count;
}

void handleMockStat(const char *path, struct stat *buf) {
    StatMockItem *conf = mockStat(path);
    if (conf == nullptr) {
        return;
    }
    buf->st_mtim.tv_sec = (conf->last_modified / 1000);
}

StatMockItem *mockStat(const char *path) {
    for (int i = 0; i < stat_mock_count; i++) {
        if (path_equal(path, stat_mock_items[i].path)) {
            return &stat_mock_items[i];
        }
    }
    return nullptr;
}

int add_keep_item(const char *path) {
    char keep_env_name[KEY_MAX];
    sprintf(keep_env_name, "V_KEEP_ITEM_%d", keep_item_count);
    setenv(keep_env_name, path, 1);
    keep_items = (PathItem *) realloc(keep_items,
                                      keep_item_count * sizeof(PathItem) + sizeof(PathItem));
    PathItem &item = keep_items[keep_item_count];
    item.path = strdup(path);
    item.size = strlen(path);
    item.is_folder = (path[strlen(path) - 1] == '/');
    return ++keep_item_count;
}

int add_forbidden_item(const char *path) {
    char forbidden_env_name[KEY_MAX];
    sprintf(forbidden_env_name, "V_FORBID_ITEM_%d", forbidden_item_count);
    setenv(forbidden_env_name, path, 1);
    forbidden_items = (PathItem *) realloc(forbidden_items,
                                           forbidden_item_count * sizeof(PathItem) +
                                           sizeof(PathItem));
    PathItem &item = forbidden_items[forbidden_item_count];
    item.path = strdup(path);
    item.size = strlen(path);
    item.is_folder = (path[strlen(path) - 1] == '/');
    return ++forbidden_item_count;
}

int add_readonly_item(const char *path) {
    char readonly_env_name[KEY_MAX];
    sprintf(readonly_env_name, "V_READONLY_ITEM_%d", readonly_item_count);
    setenv(readonly_env_name, path, 1);
    readonly_items = (PathItem *) realloc(readonly_items,
                                          readonly_item_count * sizeof(PathItem) +
                                          sizeof(PathItem));
    PathItem &item = readonly_items[readonly_item_count];
    item.path = strdup(path);
    item.size = strlen(path);
    item.is_folder = (path[strlen(path) - 1] == '/');
    return ++readonly_item_count;
}

int add_replace_item(const char *orig_path, const char *new_path) {
    //ALOGE("add replace item : %s -> %s", orig_path, new_path);
    char src_env_name[KEY_MAX];
    char dst_env_name[KEY_MAX];
    sprintf(src_env_name, "V_REPLACE_ITEM_SRC_%d", replace_item_count);
    sprintf(dst_env_name, "V_REPLACE_ITEM_DST_%d", replace_item_count);
    setenv(src_env_name, orig_path, 1);
    setenv(dst_env_name, new_path, 1);

    replace_items = (ReplaceItem *) realloc(replace_items,
                                            replace_item_count * sizeof(ReplaceItem) +
                                            sizeof(ReplaceItem));
    ReplaceItem &item = replace_items[replace_item_count];
    item.orig_path = strdup(orig_path);
    item.orig_size = strlen(orig_path);
    item.new_path = strdup(new_path);
    item.new_size = strlen(new_path);
    item.is_folder = (orig_path[strlen(orig_path) - 1] == '/');
    return ++replace_item_count;
}

void clear_replace_item() {
    replace_item_count = 0;
}

PathItem *get_keep_items() {
    return keep_items;
}

PathItem *get_forbidden_items() {
    return forbidden_items;
}

PathItem *get_readonly_item() {
    return readonly_items;
}

ReplaceItem *get_replace_items() {
    return replace_items;
}

int get_keep_item_count() {
    return keep_item_count;
}

int get_forbidden_item_count() {
    return forbidden_item_count;
}

int get_replace_item_count() {
    return replace_item_count;
}

inline bool
match_path(bool is_folder, size_t size, const char *item_path, const char *path, size_t path_len) {
    if (is_folder) {
        if (path_len < size) {
            // ignore the last '/'
            return strncmp(item_path, path, size - 1) == 0;
        } else {
            return strncmp(item_path, path, size) == 0;
        }
    } else {
        return strcmp(item_path, path) == 0;
    }
}

bool isReadOnly(const char *path) {
    for (int i = 0; i < readonly_item_count; ++i) {
        PathItem &item = readonly_items[i];
        if (match_path(item.is_folder, item.size, item.path, path, strlen(path))) {
            return true;
        }
    }
    return false;
}

const char *
reverse_relocate_path_internal(const char *path, char *const buffer, const size_t size) {
    if (path == nullptr) {
        return nullptr;
    }
    path = canonicalize_path(path, buffer, size);

    const size_t len = strlen(path);
    for (int i = 0; i < keep_item_count; ++i) {
        PathItem &item = keep_items[i];
        if (match_path(item.is_folder, item.size, item.path, path, len)) {
            return path;
        }
    }

    for (int i = 0; i < replace_item_count; ++i) {
        ReplaceItem &item = replace_items[i];
        if (match_path(item.is_folder, item.new_size, item.new_path, path, len)) {
            if (len < item.new_size) {
                return item.orig_path;
            } else {
                const size_t remain_size = len - item.new_size + 1u;
                if (size < item.orig_size + remain_size) {
                    ALOGE("reverse buffer overflow %u", static_cast<unsigned int>(size));
                    return nullptr;
                }

                const char *const remain = path + item.new_size;
                if (path != buffer) {
                    memcpy(buffer, item.orig_path, item.orig_size);
                    memcpy(buffer + item.orig_size, remain, remain_size);
                } else {
                    void *const remain_temp = alloca(remain_size);
                    memcpy(remain_temp, remain, remain_size);
                    memcpy(buffer, item.orig_path, item.orig_size);
                    memcpy(buffer + item.orig_size, remain_temp, remain_size);
                }
                return buffer;
            }
        }
    }

    return path;
}

const char *relocate_path_internal(const char *path, char *const buffer, const size_t size) {
    if (nullptr == path) {
        return path;
    }
    const char *orig_path = path;
    path = canonicalize_path(path, buffer, size);

    const size_t len = strlen(path);

    //这里是一个检测点，https://bbs.pediy.com/thread-255212.htm
    //如果先测试keep规则，再测试forbidden规则，那么无法forbid keep目录下的子目录
    //这会导致/data/data/目录可读
    // 如 /data/data/cn.hprotect.checksandbox/app_ratel_env_mock/default_0/data/.. 指向 /data/data/cn.hprotect.checksandbox/app_ratel_env_mock/default_0/ 该目录正常映射目录应该为: /data/data/
    for (int i = 0; i < forbidden_item_count; ++i) {
        PathItem &item = forbidden_items[i];
        if (match_path(item.is_folder, item.size, item.path, path, len)) {
            return nullptr;
        }
    }

    for (int i = 0; i < keep_item_count; ++i) {
        PathItem &item = keep_items[i];
        if (match_path(item.is_folder, item.size, item.path, path, len)) {
            return orig_path;
        }
    }


    for (int i = 0; i < replace_item_count; ++i) {
        ReplaceItem &item = replace_items[i];
        if (match_path(item.is_folder, item.orig_size, item.orig_path, path, len)) {
            if (len < item.orig_size) {
                // remove last /
                return item.new_path;
            } else {
                const size_t remain_size = len - item.orig_size + 1u;
                if (size < item.new_size + remain_size) {
                    ALOGE("buffer overflow %u", static_cast<unsigned int>(size));
                    return nullptr;
                }

                const char *const remain = path + item.orig_size;
                if (path != buffer) {
                    memcpy(buffer, item.new_path, item.new_size);
                    memcpy(buffer + item.new_size, remain, remain_size);
                } else {
                    void *const remain_temp = alloca(remain_size);
                    memcpy(remain_temp, remain, remain_size);
                    memcpy(buffer, item.new_path, item.new_size);
                    memcpy(buffer + item.new_size, remain_temp, remain_size);
                }
                return buffer;
            }
        }
    }
    return orig_path;
}

const char *relocate_path(const char *path, char *const buffer, const size_t size) {
//    if (path != nullptr && start_with(path, "/proc/")) {
//        ALOGI("access file:%s", path);
//    }
    return relocate_path(path, buffer, size, true);
}

const char *relocate_path(const char *path, char *const buffer, const size_t size, bool notify) {
    // ALOGI("FILE_ACCESS =%s", path);
//    if (IS_EXECVE_SUB_PROCESS) {
//        ALOGI("FILE_ACCESS =%s", path);
//    }

    const char *result = relocate_path_internal(path, buffer, size);

    if (result != nullptr && notify) {
        print_trace_if_match(result);
    }

    if (!enable_zelda) {
        return result;
    }
    if (result == nullptr) {
        return result;
    }

    //  /data/app/ratel.com.sdu.didi.gsui-U2IgvBi-UMsCksRH9rYZaQ==/base.apk does not exist
    if (start_with(result, "/data/app/")) {
        if (!start_with(result + 10, zelda_origin_package_name)) {
            return result;
        }
    }


    char *origin_pkg_start = const_cast<char *>(result), *remain_start, *remain_end;
    for (int i = 0;; i++) {
        int j = 0;
        for (; j < zelda_origin_package_name_length; j++) {
            const char now_ch = result[i + j];
            if (now_ch == '\0') {
                // /data/abc/com.tencent
                // com.tentent.mm
                return result;
            }
            if (now_ch != zelda_origin_package_name[j]) {
                break;
            }
        }
        if (j == zelda_origin_package_name_length) {
            // /data/data/com.tencent.mm/files/abc.txt
            //            com.tencent.mm
            //            1             2             3
            remain_start = origin_pkg_start + j;
            for (remain_end = remain_start; (*remain_end) != '\0'; remain_end++);
            break;
        } else {
            origin_pkg_start++;
        }
    }
    size_t remain_size = remain_end - remain_start + 1u;
    // now do replace
    if (result == buffer) {
        if (zelda_origin_package_name_length >= zelda_now_package_name_length && result != path) {
            memcpy(origin_pkg_start, zelda_now_package_name, zelda_now_package_name_length);
            memcpy(origin_pkg_start + zelda_now_package_name_length, remain_start, remain_size);
        } else {
            void *const remain_temp = alloca(remain_size);
            memcpy(remain_temp, remain_start, remain_size);
            memcpy(origin_pkg_start, zelda_now_package_name, zelda_now_package_name_length);
            memcpy(origin_pkg_start + zelda_now_package_name_length, remain_temp, remain_size);
        }
    } else {
        size_t prefix_size = origin_pkg_start - result;
        memcpy(buffer, result, prefix_size);
        memcpy(buffer + prefix_size, zelda_now_package_name, zelda_now_package_name_length);
        memcpy(buffer + prefix_size + zelda_now_package_name_length, remain_start, remain_size);
        result = buffer;
    }

//    ALOGI("before relocate =%s", path);
//    ALOGI("after relocate =%s", result);
    return result;
}

const char *reverse_relocate_path(const char *path, char *const buffer, const size_t size) {
    const char *result = reverse_relocate_path_internal(path, buffer, size);
    if (!enable_zelda) {
        return result;
    }

    if (result == nullptr) {
        return result;
    }
    // /data/app/virjar.zelda.ctripandroidview.zElDa.debug191-jOs6p42UAFyMZZPXJszM5g==/lib/arm -> /data/app/ctrip.android.view-jOs6p42UAFyMZZPXJszM5g==/lib/arm


    char *now_pkg_start = const_cast<char *>(result), *remain_start, *remain_end;
    for (int i = 0;; i++) {
        int j = 0;
        for (; j < zelda_now_package_name_length; j++) {
            const char now_ch = result[i + j];
            if (now_ch == '\0') {
                // /data/abc/com.tencent
                // com.tentent.mm
                return result;
            }
            if (now_ch != zelda_now_package_name[j]) {
                break;
            }
        }
        if (j == zelda_now_package_name_length) {
            // /data/data/com.tencent.mm/files/abc.txt
            //            com.tencent.mm
            //            1             2             3
            remain_start = now_pkg_start + j;
            for (remain_end = remain_start; (*remain_end) != '\0'; remain_end++);
            break;
        } else {
            now_pkg_start++;
        }
    }
    size_t remain_size = remain_end - remain_start + 1u;
    // now do replace
    if (result == buffer) {
        if (zelda_now_package_name_length >= zelda_origin_package_name_length && result != path) {
            memcpy(now_pkg_start, zelda_origin_package_name, zelda_origin_package_name_length);
            memcpy(now_pkg_start + zelda_origin_package_name_length, remain_start, remain_size);
        } else {
            void *const remain_temp = alloca(remain_size);
            memcpy(remain_temp, remain_start, remain_size);
            memcpy(now_pkg_start, zelda_origin_package_name, zelda_origin_package_name_length);
            memcpy(now_pkg_start + zelda_origin_package_name_length, remain_temp, remain_size);
        }
    } else {
        size_t prefix_size = now_pkg_start - result;
        memcpy(buffer, result, prefix_size);
        memcpy(buffer + prefix_size, zelda_origin_package_name, zelda_origin_package_name_length);
        memcpy(buffer + prefix_size + zelda_origin_package_name_length, remain_start, remain_size);
        result = buffer;
    }

    return result;
}

int reverse_relocate_path_inplace(char *const path, const size_t size) {
    char path_temp[PATH_MAX];
    const char *redirect_path = reverse_relocate_path(path, path_temp, sizeof(path_temp));
    if (redirect_path) {
        if (redirect_path != path) {
            const size_t len = strlen(redirect_path) + 1u;
            if (len <= size) {
                memcpy(path, redirect_path, len);
            }
        }
        return 0;
    }
    return -1;
}