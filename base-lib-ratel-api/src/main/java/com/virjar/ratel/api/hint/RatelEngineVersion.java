package com.virjar.ratel.api.hint;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记该API从那个版本的引擎开始支持，ratelAPI和引擎关系密切
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface RatelEngineVersion {
    String value() default RatelEngineHistory.V_1_2_5;
}
