package com.dong6662.minispring.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DynamicProxy implements InvocationHandler {
    private Object target;
    private Method before;
    private Method after;
    private Object aspect;
    private String targetMethod;

    /**
     * 万一这个被代理对象没有实现接口呢？好像原项目里面没有说。
     * 哦，原项目里面其实是说了的：接口一定要定义，否则无法使用 AOP，因为我们使用的是 JDK 动态代理，只能代理实现了接口的类。来源： https://www.cnblogs.com/czwbig/p/11159946.html
     * 其实过后可以尝试使用cglib这个方式
     * @param target 被代理对象（目标对象）
     * @param targetMethod 目标方法，被切入（或说被代理）的方法
     * @param aspect 切面类
     * @param before 切面类中的前置处理器
     * @param after 切面类中的后置处理器
     * @return 代理对象
     */
    public Object createProxy(Object target, Method before, Method after, Object aspect, String targetMethod){
        this.target=target;
        this.before=before;
        this.after=after;
        this.aspect=aspect;
        this.targetMethod=targetMethod;
        return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 我们只需要对 targetMethod 方法进行处理，其他方法直接返回原来的调用
        if (!method.getName().equals(this.targetMethod)) return method.invoke(this.target,args);

        // before和after都是aspect切面对象内部的方法，小心调用错了
        // 实际上这些切面类中的方法都得有参数的，但是这个简易版的spring框架没有做这个功能，但是可以日后添砖加瓦
        // 细节要注意，前置处理器和后置处理器都加了null判断的
        if (before!=null)
            before.invoke(this.aspect);
        Object result = method.invoke(target,args);
        if (after!=null)
            after.invoke(this.aspect);
        return result;
    }
}
