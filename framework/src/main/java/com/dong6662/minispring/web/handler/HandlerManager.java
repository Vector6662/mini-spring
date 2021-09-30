package com.dong6662.minispring.web.handler;

import com.dong6662.minispring.web.mvc.Controller;
import com.dong6662.minispring.web.mvc.RequestMapping;
import com.dong6662.minispring.web.mvc.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandlerManager {
    // 类中各 @RequestMapping 注解修饰的方法对应的 MappingHandler 的集合
    public static List<MappingHandler> mappingHandlerList = new ArrayList<>();

    public static void resolveMappingHandler(List<Class<?>> classList){
        // steam yyds！
        classList.stream()
                .filter(aClass -> aClass.isAnnotationPresent(Controller.class))
                .forEach(HandlerManager::parseHandlerFromController);
    }

    /**
     * 该类的目的是找到这些有@Controller注解的类中有@RequestMapping，获取uri、该方法的参数等等，目的只有一个：构造一个MappingHandler对象
     * @param controllerClass 有@Controller注解的class对象
     */
    private static void parseHandlerFromController(Class<?> controllerClass){
        // 还是学学原项目中采用的流式编程，的确很简洁高效的编程方式啊！JDK8 yyds！
        Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .forEach(method -> {
                    String uri = method.getAnnotation(RequestMapping.class).value();
                    String[] paramsStr = Arrays.stream(method.getParameters())
                            .filter(parameter -> parameter.isAnnotationPresent(RequestParam.class))
                            .map(parameter -> parameter.getAnnotation(RequestParam.class).value())
                            /*
                             * 下面是什么神仙语法？查了一下相当于：
                             * public String[] create(int size) {return new String[size];}
                             * */
                            .toArray(String[]::new);
                    // 到此为止，已经拿到了能够构造MappingHandler的所有参数！
                    MappingHandler handler = new MappingHandler(uri,controllerClass,method,paramsStr);
                    HandlerManager.mappingHandlerList.add(handler);
                });
    }
}
