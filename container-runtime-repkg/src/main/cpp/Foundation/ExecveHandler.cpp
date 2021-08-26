//
// Created by 邓维佳 on 2019-09-28.
//

#include <cstdlib>
#include <zconf.h>
#include <asm/unistd.h>
#include <asm/fcntl.h>
#include <fstream>
#include <fcntl.h>
#include "ExecveHandler.h"
#include "Log.h"
//#include "ratel_build_type.h"


ExecveChainHandler root = ExecveChainHandler();

void register_execve_handler(ExecveChainHandler *handler) {
    root.addHandler(handler);
}

//int (*origin_execve)(const char *pathname, char *const argv[], char *const envp[]);
int handle_execve(const char *pathname, char *argv[], char *const envp[]) {
    ExecveParam execveParam = ExecveParam();
    execveParam.pathname = pathname;
    execveParam.argv = argv;
    execveParam.envp = envp;
    return root.doExeceve(execveParam);
}

bool ExecveChainHandler::beforeExeceve(ExecveParam *execveParam) {
    return true;
}

void
ExecveChainHandler::afterExeceve(ExecveParam originExecveParam, ExecveParam afterExecveParam) {}

int ExecveChainHandler::doExeceve(ExecveParam execveParamp) {
    //请注意谨慎添加日志，不是所有的命令都导入的Android的日志框架，擅自输出日志很可能导致命令无法执行
    ExecveParam beforeParam = ExecveParam();
    beforeParam.pathname = execveParamp.pathname;
    beforeParam.argv = execveParamp.argv;
    beforeParam.envp = execveParamp.envp;

    ExecveParam afterParam = ExecveParam();
    afterParam.pathname = execveParamp.pathname;
    afterParam.argv = execveParamp.argv;
    afterParam.envp = execveParamp.envp;

    bool exe_next = beforeExeceve(&afterParam);
    if (this->next == nullptr) {
        exe_next = false;
    }
    int ret;
    if (afterParam.returnEarly) {
        ret = afterParam.ret;
    } else if (exe_next) {
        ret = this->next->doExeceve(afterParam);
    } else {
        ret = static_cast<int>(syscall(__NR_execve, afterParam.pathname, afterParam.argv,
                                       afterParam.envp));
    }

    afterExeceve(beforeParam, afterParam);
    return ret;
}


bool ExecveTraceHandler::beforeExeceve(ExecveParam *execveParam) {
    char pattern[PATH_MAX] = {0};
    char *cache_dir = getenv("RATEL_NATIVE_PATH");
    snprintf(pattern, sizeof(pattern), "%s/execeve_track_%d_%d.txt", cache_dir, getpid(), gettid());
    FILE *fp = fopen(pattern, "a+");
    if (fp == nullptr) {
        return true;
    }

    fputs("cmd:", fp);
    fputs(execveParam->pathname, fp);
    fputs("\nargs:", fp);
    char *const *ptr = execveParam->argv;
    while ((*ptr) != nullptr) {
        fputs(*ptr, fp);
        fputs("\n", fp);
        ptr++;
    }
    fputs("\nenv:", fp);
    ptr = execveParam->envp;
    while ((*ptr) != nullptr) {
        fputs(*ptr, fp);
        fputs("\n", fp);
        ptr++;
    }
    fputs("\n\n", fp);
    fclose(fp);
    return true;
}