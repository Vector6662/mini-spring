package com.dong6662.minispring.web.servlet;


import com.dong6662.minispring.web.handler.HandlerManager;
import com.dong6662.minispring.web.handler.MappingHandler;


import javax.servlet.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class DispatcherServlet implements Servlet {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    /**
     * 这是所有请求的入口，看看参数就知道了！TomcatServer#startServer中会add这个Servlet了，因为它有下边这两个参数，能够处理
     * http请求
     * @param servletRequest
     * @param servletResponse
     * @throws IOException
     */
    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        // 从所有的 MappingHandler 中逐一尝试处理请求，如果某个 handler 可以处理(返回true)，则返回即可
        for (MappingHandler handler: HandlerManager.mappingHandlerList){
            try {
                if(handler.handle(servletRequest, servletResponse))
                    return;
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        servletResponse.getWriter().write("failed to handle this request");
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
