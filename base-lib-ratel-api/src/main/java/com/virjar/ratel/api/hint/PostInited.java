package com.virjar.ratel.api.hint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记这个接口(或者数据)在框架启动后才可以调用，也就是无法在
 * public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable 函数体中使用
 * 具体原因需参见ratel框架生命周期介绍
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface PostInited {
}
