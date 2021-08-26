//
// Created by 能链集团 on 2021/6/28.
//

#include <fstream>
#include <iostream>
#include <unistd.h>
#include <dirent.h>
#include <android/log.h>

int main(int argc, char **argv) {
    char *pidDir = argv[1];
    // 先休眠5秒，因为java端3秒就会删除对应的文件
    __android_log_print(ANDROID_LOG_INFO, "RATELANR", "pidDir : %s", pidDir);
    sleep(5);
    __android_log_print(ANDROID_LOG_INFO, "RATELANR", "sleep finish ,start detect anr");
    DIR *dir = opendir(pidDir);
    struct dirent *file;
    // read all the files in dir
    while ((file = readdir(dir)) != nullptr) {
        // skip "." and ".."
        if (strcmp(file->d_name, ".") == 0 || strcmp(file->d_name, "..") == 0) {
            continue;
        }
        if (file->d_type == DT_DIR) {
            continue;
        }
        __android_log_print(ANDROID_LOG_INFO, "RATELANR", "kill -9 %s because of anr",
                            file->d_name);
        kill((int) (strtol(file->d_name, nullptr, 10)), 9);
        char path[1024];
        sprintf(path, "%s/%s", pidDir, file->d_name);
        remove(path);
    }
    closedir(dir);
}