package com.dong6662.minispring.aop;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Pointcut {
    /**
     * 定义切点的名字，spring中这里还会用到 execution() 等表达式来解析
     * 简单起见，我们只能定义一个字符串表示切点位置，
     * 如 com.caozhihu.demo.Rapper.rap()
     *
     * 暂时先复制原项目中的注释
     */
    String target() default "";
}
