package com.virjar.ratel.api;

import com.virjar.ratel.api.hint.RatelEngineHistory;
import com.virjar.ratel.api.hint.RatelEngineVersion;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@RatelEngineVersion(RatelEngineHistory.V_1_2_8)
public interface DexMakerProxyBuilderHelper {
    /**
     * 创建一个aop代理构造器
     *
     * @param clazz 对应class，如代理InputStream
     * @param <T>   任意class类型
     * @return builder实例
     */
    <T> DexMakerProxyBuilder<T> forClass(Class<T> clazz);

    /**
     * 判断一个class是不是aop代理产生的class
     *
     * @param c 待测试的class
     * @return 测试结果，是否为代理class
     */
    boolean isProxyClass(Class<?> c);

    /**
     * 调用supper方法，由于我们继承了方法，在AOP场景下，我们还需要call origin。我们可能只是修改参数或者拦截返回值
     *
     * @param proxy  当前的代理对象
     * @param method 当前调用方法
     * @param args   参数
     * @return 方法调用结果
     * @throws Throwable 可能抛出异常
     */
    Object callSuper(Object proxy, Method method, Object... args) throws Throwable;

    /**
     * InvocationHandler是动态代理机制里面很重要的一个概念，通过他处理AOP的实际业务逻辑，一般很少使用
     *
     * @param instance 代理对象
     * @return 获取对应的 InvocationHandler
     */
    InvocationHandler getInvocationHandler(Object instance);

    /**
     * 给代理对象替换handler，一般很少使用
     *
     * @param instance 代理对象
     * @param handler  新的handler
     */
    void setInvocationHandler(Object instance, InvocationHandler handler);
}
