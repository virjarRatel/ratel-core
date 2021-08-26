package com.virjar.ratel.api;

import java.lang.reflect.Method;

/**
 * native分析相关的帮助类
 */
public interface NativeHelper {

    public class NativePrtInfo {
        /**
         * native的函数指针
         */
        public long ptr;
        /**
         * native函数相对于so的偏移量
         */
        public long offset;

        /**
         * native函数对应的so文件地址
         */
        public String soPath;
    }

    /**
     * 查询native方法指针在的so相关信息，包括函数指针、函数对应so的偏移量、so对应的文件地址。这些数据可能获取包（时机不正确、内存匿名映射等）
     *
     * @param method method对象，必须是native的
     * @return NativePrtInfo结构体
     */
    NativePrtInfo queryMethodNativeInfo(Method method);


    /**
     * 尝试将一个native方法的so文件dump下来
     *
     * @param method native方法
     * @return so文件。有可能识别失败，此时返回null
     */
    byte[] dumpSo(Method method);

    /**
     * dump读取内存中的数据
     *
     * @param start 开始地址
     * @param size  数据大小
     * @return dump的数据内容
     */
    byte[] dumpMemory(long start, long size);

    /**
     * 监控应用程序的读写操作
     *
     * @param feature 包含的特定字符串
     * @param mode    监控模式 {@link TraceFileMode}
     */
    void traceFile(String feature, int mode);

    /**
     * 监控应用程序的读写操作
     *
     * @param feature        包含的特定字符串
     * @param mode           监控模式 {@link TraceFileMode}
     * @param fileOpCallback 当文件读写操作发生时的回调
     */
    void traceFile(String feature, int mode, FileOpCallback fileOpCallback);

    interface FileOpCallback {
        void onFileOp(int mode, String path);
    }

    enum TraceFileMode {
        READ,
        WRITE,
        OPEN
    }
}
