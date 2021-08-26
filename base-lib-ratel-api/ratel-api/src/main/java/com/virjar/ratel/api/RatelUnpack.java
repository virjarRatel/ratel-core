package com.virjar.ratel.api;

import java.io.File;
import java.lang.reflect.Member;
import java.util.List;

/**
 * 脱壳机实现，支持指令抽取和修复、支持多classLoader、支持热加载classdump、
 * 不支持vmp、不支持主动调用(可以在业务层模拟主动调用)
 */
public interface RatelUnpack {

    /**
     * 获取当前的工作目录，在enableUnPack调用之后才会有值
     *
     * @return 脱壳机工作目录
     */
    File getWorkDir();

    /**
     * 开启脱壳机，ratel框架将会影响虚拟机代码执行流程。这可能导致框架不稳定，已经影响app执行性能<br>
     * 一般情况不建议随时开启脱壳机<br>
     * 请注意，脱壳机开启需要在app运行前执行，否则错过dump时间
     *
     * @param workDir    需要指定一个工作目录，让脱壳机dump相关加密的指令.参数可以为空，为空系统自动分配
     * @param dumpMethod 是否需要dump指令，大部分情况下dex整体dump就可以，此时不存在dex修复过程，相对来说性能更好
     */
    void enableUnPack(File workDir, boolean dumpMethod);


    /**
     * 根据一个className搜索dex，在存在热修复等场景下可能有多个dex，每个dex使用字节数组传递二进制内容
     *
     * @param className 一个特定的class
     * @return 包含这个class定义的dex文件，可能有多个
     */
    List<byte[]> findDumpedDex(String className);


    /**
     * 根据一个method，直接获取他在内存中的dex镜像数据，在不存在指令修复的场景下，直接调用这个便可以精准脱壳
     *
     * @param method 一个特定的方法，可以通过各种hook手段、反射手段获取到
     * @return 内存数据
     */
    byte[] methodDex(Member method);
}
