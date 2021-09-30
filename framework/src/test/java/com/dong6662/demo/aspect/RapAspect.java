package com.dong6662.demo.aspect;

import com.dong6662.minispring.aop.After;
import com.dong6662.minispring.aop.Aspect;
import com.dong6662.minispring.aop.Before;
import com.dong6662.minispring.aop.Pointcut;
import com.dong6662.minispring.bean.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class RapAspect {
    private Logger log = LoggerFactory.getLogger(RapAspect.class);

    @Pointcut(target = "com.dong6662.demo.service.Rapper.rap")
    public void rapPoint(){
    }

    @Before("rapPoint()")
    public void singAndDance(){
        log.info("before rap: sing and dance");
    }

    @After("rapPoint()")
    public void basketball(){
        log.info("after rap: basketball");
    }


}
