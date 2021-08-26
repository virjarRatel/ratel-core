package com.virjar.ratel.envmock.binder;

public interface BindMethodHook {
    /**
     * ipc 调用之前
     *
     * @param args ipc的参数，来自aidl生成函数入口参数
     */
    void beforeIpcCall(Object[] args);

    /**
     * ipc调用之后
     *
     * @param args   ipc的参数，来自aidl生成函数入口参数
     * @param result ipc调用的结果，来自aidl生成的函数声明
     * @return 被hook逻辑替换的结果，请注意如果没有修改，那么需要原样返回result，不可以返回null
     */
    Object afterIpcCall(Object[] args, Object result);
}
