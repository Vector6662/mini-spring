package com.dong6662.minispring.web.handler;

import cn.hutool.core.util.ObjectUtil;
import com.dong6662.minispring.bean.BeanFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MappingHandler {
    private String uri;
    private Class<?> controller;
    private Method method;
    private String[] args;
    /**
     * 这个类我感觉算是个封装类，表明某个uri请求交由哪个Controller的哪个Method处理，并且封装了这个uri请求的参数，一个MappingHandler处理一个uri
     * @param uri
     * @param method
     * @param controller
     * @param args 参数变量名列表，是@RequestParam("userID")中的userId，是一个String类型的变量
     */
    public MappingHandler(String uri, Class<?> controller, Method method, String[] args) {
        this.uri = uri;
        this.controller = controller;
        this.method = method;
        this.args = args;
    }
    public boolean handle(ServletRequest request, ServletResponse response) throws InvocationTargetException, IllegalAccessException, IOException {
        //由于一个MappingHandler对象只能处理一个uri的请求，所以要先判断这个request的请求地址是不是这个MappingHandler能够处理的
        String requestURI = ((HttpServletRequest) request).getRequestURI();
        if(!this.uri.equals(requestURI)) return false;

        Object[] parameters = new Object[this.args.length];
        for (int i = 0; i < parameters.length; i++)
            parameters[i]=request.getParameter(this.args[i]);

        // 我又一次体会到反射的强大之处了，从bean容器中获取了对应的bean对象之后通过调用method.invoke()就不用考虑类型转换的问题了，反正都是Object类型
        // 对了不要忘了这个this.method一定要是targetController内部的对象，否则会出大问题
        Object targetController = BeanFactory.getBean(controller);
        Object result = this.method.invoke(targetController, parameters);
        if (ObjectUtil.isNull(result)){
            response.getWriter().write("no reply");
        } else{
            response.getWriter().write(result.toString());
        }
        return true;
    }
}
