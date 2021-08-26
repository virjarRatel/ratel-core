package com.virjar.ratel.api.scheduler;

import java.util.Map;

public interface RatelTask {

    /**
     * 加载调度任务参数，每次调度任务执行之前，可能需要初始化任务相关参数。
     * <br>
     * 非常重要的是，这次调用发生在manager进程,你不能将加载好的数据放到静态变量或者当前进程内存中，否则整个调度过程可能由于app重启导致内存数据丢失
     *
     * @return 任务参数map, KV均为字符串,可为空
     */
    Map<String, String> loadTaskParams();

    /**
     * 执行调度任务
     * <br>
     * 该任务发生在slave app中
     *
     * @param params 调度任务参数
     */
    void doRatelTask(Map<String, String> params);
}
