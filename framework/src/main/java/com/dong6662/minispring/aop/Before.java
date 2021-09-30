package com.dong6662.minispring.aop;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Before {
    // 需要织入的切入点，由 @Pointcut 定义
    String value() default "";
}
