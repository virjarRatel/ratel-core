//
// Created by 邓维佳 on 2019-09-28.
//


#ifndef EXECVE_HANDLER_H
#define EXECVE_HANDLER_H

struct ExecveParam {
    bool returnEarly;
    int ret;
    const char *pathname;
    char **argv;
    char *const *envp;

};

class ExecveChainHandler {
protected:
    virtual bool beforeExeceve(ExecveParam *execveParam);

    virtual void afterExeceve(ExecveParam originExecveParam, ExecveParam afterExecveParam);

private:
    ExecveChainHandler *next = nullptr;


public:
    int doExeceve(ExecveParam execveParam);

    void addHandler(ExecveChainHandler *handler) {
        if (handler == nullptr) {
            return;
        }
        if (this == handler) {
            //不准重复注册
            return;
        }
        if (next == nullptr) {
            next = handler;
        } else {
            next->addHandler(handler);
        }

    }
};

//开发版本下，会trace所有的跨进程调用命令，用于观察app设备指纹通过命令行做了什么事情
class ExecveTraceHandler : public ExecveChainHandler {
protected:
    bool beforeExeceve(ExecveParam *execveParam);

    void afterExeceve(ExecveParam originExecveParam, ExecveParam afterExecveParam) {

    }
};

void register_execve_handler(ExecveChainHandler *handler);

//int (*origin_execve)(const char *pathname, char *const argv[], char *const envp[]);
int handle_execve(const char *pathname, char *argv[], char *const envp[]);

#endif