package com.dong6662.minispring.web.server;

import com.dong6662.minispring.web.servlet.DispatcherServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TODO: 2021/5/14 这段代码目前不是核心部分，后面找时间来研究一下
public class TomcatServer {
    private static Logger logger = LoggerFactory.getLogger(TomcatServer.class);
    private Tomcat tomcat;
    private String[] args;

    public TomcatServer(String[] args){
        this.args = args;
    }

    public void startServer() {
        Tomcat tomcat = new Tomcat();
        //设置connect
        Connector connector = new Connector();
        connector.setURIEncoding("UTF-8");
        connector.setPort(8088);
        tomcat.getService().addConnector(connector);


        Context context = tomcat.addContext("/", null);
        Tomcat.addServlet(context, "DispatcherServlet", new DispatcherServlet());
        context.addServletMappingDecoded("/", "DispatcherServlet");
        try {
            tomcat.start();
            logger.info("tomcat started at port 8088");
//            tomcat.getServer().await();
        } catch (LifecycleException e) {
            logger.error("tomcat failed to start");
            e.printStackTrace();
        }


    }
}
