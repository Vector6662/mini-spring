package com.dong6662.minispring.bean;

import java.lang.annotation.*;

@Documented
@Retention(value = RetentionPolicy.RUNTIME)//印象中好像用注解实现反射，retention（保持）就得设置为RUNTIME
@Target(ElementType.FIELD)//注意哦，@AutoWired肯定都是在类属性上注解的，所以这里毫无疑问设置为FIELD
public @interface AutoWired {

}
