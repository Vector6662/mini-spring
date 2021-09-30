package com.dong6662.minispring.bean;

import cn.hutool.core.util.ObjectUtil;
import com.dong6662.minispring.aop.*;
import com.dong6662.minispring.transaction.TransactionManager;
import com.dong6662.minispring.transaction.Transactional;
import com.dong6662.minispring.web.mvc.Controller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {
    //bean容器
    private static Map<Class<?>, Object> beans = new ConcurrentHashMap<>();

    private static Set<Class<?>> beansHaveAutoWiredField = Collections.synchronizedSet(new HashSet<>());

    /**
     * 初始化 Bean 工厂,初始化 AOP，这里使用了 JDK 动态代理。Bean工厂第一次初始化后，使用代理类的对象来覆盖 Bean 工厂中的对应对象。
     * 上面这句话是原项目中写的，对后面这句“Bean工厂第一次初始化后，使用代理类的对象来覆盖 Bean 工厂中的对应对象” 有疑问。感觉好像并不是这么做的。待我再仔细看看
     *
     * 做三件事：1.将@Component、@Controller注解的类添加到Bean容器中；2.将@Aspect注解的类添加到bean容器中，并实现@before和@after代理；3.@Autowired注入
     *
     * 原项目中三件事情的流程是：首先过滤出有@Aspect注解的Class，暂时不创建bean对象，
     * 我认为可以把流程优化为：原项目的逻辑是，创建bean实例后紧接着就设置属性，这样会导致循环依赖的问题。为了解决这个问题，可以先创建bean，然后统一设置这些bean的Autowired属性
     * @param classes
     */
    public static void initBean(List<Class<?>> classes) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        //根据以前编程的经验，这里仍然是浅拷贝，只是一些变量指向这些classes对象。
        List<Class<?>> classesToCreate = new ArrayList<>(classes);

        //被@Aspect注解的切面类
        List<Class<?>> aspectClasses = new ArrayList<>();
        List<Class<?>> transactionClasses = new ArrayList<>();

        //过滤出aspectClasses，暂时不创建bean，其他的class正常创建bean
        for (Class<?> cls:classesToCreate){
            if (cls.isAnnotationPresent(Aspect.class)){
                aspectClasses.add(cls);
            }
            if (cls.isAnnotationPresent(Transactional.class)){
                transactionClasses.add(cls);
            }
            createBean(cls);
        }

        //通过上面的循环就可以找出有Aspect注解的类了，现在就需要实现AOP
        resolveAOP(aspectClasses);

        //处理事务
        resolveTransaction(transactionClasses);



        // 所有bean实例，包括需要代理类的bean对象都已经创建完毕，现在需要处理他们的AutoWired注解
        for (Class<?> clazz:beansHaveAutoWiredField)
            resolveAutoWired(clazz);


        beans.getClass();//这里用来打断点

    }

    /**
     * 创建bean对象，创建的这些bean对象还未设置他们有Autowired注解的属性
     * @param clazz
     */
    private static void createBean(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        // TODO: 2021/6/26 目前有Aspect注解的类也需要再注解一下Component，这有点繁琐，可以优化一下
        // 只处理@Component和@Controller注解的类
        if(!clazz.isAnnotationPresent(Controller.class)
                &&!clazz.isAnnotationPresent(Component.class))
            return;

        // 初始化这个bean
//        Object bean = clazz.getDeclaredConstructor().newInstance(); // 这是jdk9后推荐的写法，代替clazz.newInstance()这种显得粗狂的写法
        Object bean = clazz.newInstance();//思考这个方法被废弃的原因（用JDK8的时候好像还没有被废弃，到了11就被废弃了），其实这个方法的文档中给出了答案只不过没做怎么看懂...

        // 一旦发现这个类有AutoWired注解，就将这个类记录一下，最后统一处理
        for(Field field: clazz.getDeclaredFields()){
            if (field.isAnnotationPresent(AutoWired.class)) {
                BeanFactory.beansHaveAutoWiredField.add(clazz);
                break;
            }
        }
        beans.put(clazz,bean);
    }

    /**
     * 目前只实现了jdk的动态代理，所以被代理类必须实现接口
     * @param aspectClasses
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static void resolveAOP(List<Class<?>> aspectClasses) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        //感觉这一句其实可以省略？
        if (aspectClasses.size()==0)
            return;

        for (Class<?> aspectClass:aspectClasses) {
            //因为最后要调用DynamicProxy的createProxy()，所以每一个for循环就是为了获取这些参数的，这样说的话这么长一截for循环代码看起来也没那么复杂了
            Object target = null;
            Object aspect;
            String pointcutMethod = null;
            String targetMethod = null;
            Method before = null;
            Method after = null;

            aspect = aspectClass.newInstance();

            // 假设@Pointcut注解在RapAspect类的rapPoint()方法上，value=com.caozhihu.demo.Rapper.rap()可以获取到的信息有：
            // 切入点方法pointcutMethod为rapPoint()，目标对象target为com.caozhihu.demo.Rapper；目标方法targetMethod为rap()
            for (Method method: aspectClass.getDeclaredMethods()){
                //首先得找到有@Pointcut注解的方法，不然没得玩
                if (!method.isAnnotationPresent(Pointcut.class))
                    continue;

                // 通过@PointCut获取目标对象target以及目标方法targetMethod
                String pointCut = method.getAnnotation(Pointcut.class).target();//com.dong6662.demo.service.Rapper.rap
                String targetClassName = pointCut.substring(0,pointCut.lastIndexOf('.'));////com.dong6662.demo.service.Rapper
                Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(targetClassName);//com.dong6662.demo.service.Rapper的Class对象
                target = targetClass.newInstance();//com.dong6662.demo.service.Rapper实例，也就是我们要的目标对象

                // TODO: 2021/6/24 如果pointCut最后有括号“ () ”，为了保证代码健壮性可以增加括号判断（Postel’s Law）
                targetMethod = pointCut.substring(pointCut.lastIndexOf('.')+1);//rap，没有括号（rap()）的哦！
                pointcutMethod = method.getName();//rapPoint，也是没有括号
                break;//加这个break应该没问题嘛，反正一个切面类假定只有一个PointCut。
            }
            // 原文中说，设定每一个切面类(Aspect)，最多只有一个切点(Pointcut)、一个前置以及一个后置处理器，所以我们也必需先处理 pointcut，再解析before和after方法。
            // 我本来想直接将获取目标方法和切点的过程和获取before、after的都写在上面的一个for循环中，但是发现得首先获取到pointcut，后续的before和after才有变量用，不信你看：

            // TODO: 2021/5/16 这里我觉得应该加一条判断：如果没有找到织入点(Pointcut) 那就直接抛出一个异常：没有设置PointCutException

            // 寻找前置处理器before和后置处理器after
            for (Method method: aspectClass.getDeclaredMethods()){
                if(method.isAnnotationPresent(Before.class)){
                    String value = method.getAnnotation(Before.class).value();//这个变量描述的是pointcutMethod，即rapPoint()
                    value = value.substring(0,value.lastIndexOf('('));
                    if (value.equals(pointcutMethod))
                        before = method;
                } else if (method.isAnnotationPresent(After.class)){
                    String value = method.getAnnotation(After.class).value();
                    value = value.substring(0,value.lastIndexOf('('));
                    if(value.equals(pointcutMethod))
                        after = method;
                }
            }
            Object proxy = new DynamicProxy().createProxy(target,before,after,aspect,targetMethod);
            // BeanFactory.beans.put(aspectClass,proxy); 这是我自己制造的错误，put()中的key应该是目标对象target所属的类
            BeanFactory.beans.put(target.getClass(),proxy);
        }

    }

    /**
     * 处理有AutoWired注解的bean
     * @param clazz
     */
    private static void resolveAutoWired(Class<?> clazz) throws IllegalAccessException {
        Object bean = getBean(clazz);
        for (Field field:bean.getClass().getDeclaredFields()){
            if (!field.isAnnotationPresent(AutoWired.class)) continue;
            Class<?> fieldType = field.getType();
            // 属性为Interface需要特殊处理，在beans中找到其实现类
            if (fieldType.isInterface()){
                for (Class<?> key:BeanFactory.beans.keySet()){
                    if (fieldType.isAssignableFrom(key)){
                        fieldType=key;
                        break;
                    }
                }
            }
            //因为有的属性可能是非public访问权限
            field.setAccessible(true);
            field.set(bean,getBean(fieldType));
        }

    }

    public static Object getBean(Class<?> clazz){
        Object bean = BeanFactory.beans.get(clazz);
        if (ObjectUtil.isNull(bean))
            throw new IllegalArgumentException("no bean named"+clazz.getName()+"is defined");
        return bean;
    }

    private static void resolveTransaction(List<Class<?>> transactionClasses){
        for (Class<?> transactionClass : transactionClasses) {
            Object o = new TransactionManager().transactionProxy(transactionClass);
            beans.put(o.getClass().getSuperclass(),o);
        }

    }
}
