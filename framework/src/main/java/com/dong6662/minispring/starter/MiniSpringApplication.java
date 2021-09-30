package com.dong6662.minispring.starter;

import com.dong6662.minispring.bean.BeanFactory;
import com.dong6662.minispring.core.ClassScanner;
import com.dong6662.minispring.web.handler.HandlerManager;
import com.dong6662.minispring.web.server.TomcatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

public class MiniSpringApplication {
    private final static Logger logger = LoggerFactory.getLogger(MiniSpringApplication.class);


    /**
     * 从这里看框架的逻辑还是很清晰的
     * @param clazz
     * @param args
     */
    public static void run(Class<?> clazz, String[] args) {
        TomcatServer tomcatServer = new TomcatServer(args);
        try{

            //扫描项目下的所有.class文件，这也是为什么一个项目的启动类要放在最外层
            //clazz.getPackageName();我感觉应该也可以
            List<Class<?>> classes = ClassScanner.scanClasses(clazz.getPackage().getName());

            //初始化bean容器，并实现aop
            BeanFactory.initBean(classes);


            //
            HandlerManager.resolveMappingHandler(classes);

            //启动服务
            tomcatServer.startServer();
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }

}
