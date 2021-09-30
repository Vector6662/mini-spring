package com.dong6662.minispring.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {


    /**
     * 加载指定包名下的类，例如com.dong6662.spring
     * @param packageName
     * @return
     */

    public static List<Class<?>> scanClasses(String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.','/');

        // 线程上下文类加载器默认是应用类加载器，即 ClassLoader.getSystemClassLoader();
        // 而且`AppClassloader`主要也是用来加载 自己编写的代码和第三方jar包，且pom.xml文件下依赖涉及的类都得用这个加载器加载；
        // 参考：https://www.zhihu.com/question/46719811/answer/1739289578
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        //好像这里还不能使用增强for循环，但是可以把枚举类理解成低配版的Iterator
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            // TODO: 2021/5/14 我感觉得debug一下，看看getProtocol()返回的是些啥玩意儿。
            //  答，String类型，文件的名称。大部分一个都是file，jar文件的protocol居然就是jar，可能人家本身就是一种协议？和http这种一样？
            //看代码目前可以知道逻辑是得到一个一个的jar包的路径，然后将这个jar包下的.class文件取出来，这个任务交给了getClassesFromJar()
            //上面的理解是错误的，因为这个错误的理解让我在这里卡壳了好久。正确的理解应该是，将整个jar文件路径传入getClassesFromJar方法，
            // 是这个方法内解析的到所有的class文件，而不是在这个URL的枚举类的遍历里边解析的！
            if (url.getProtocol().contains("jar")){
                //我在README中翻译了一下JarURLConnection的文档，但目前还只是一知半解
                JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                // TODO: 2021/5/15 这里既然可以通过JarURLConnection得到一个JarFile，那么为什么不直接将这个JarFile实例传递给 getClassesFromJar()呢？
                //  因为在这个方法内还是会将jarFilePath转换为jarFile，这有点多费一到手的感觉
                String jarFilePath = jarURLConnection.getJarFile().getName();
                classes.addAll(getClassesFromJar(jarFilePath,path));
            } else if (url.getProtocol().contains("file")){
                classes.addAll(getClassesFromFile(url, packageName));
            }

        }
        return classes;
    }

    /**
     * 读取给定路径jar包中的.class文件
     * @param jarFilePath
     * @param path
     * @return
     */
    private static List<Class<?>> getClassesFromJar(String jarFilePath, String path) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        JarFile jarFile = new JarFile(jarFilePath);
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();

            String entryName = jarEntry.getName();
            //只读取path下的.class文件
            if (entryName.startsWith(path)&&entryName.endsWith(".class")){
                //类的全限定名没有".class"
                String classBinaryName = entryName.replace('.','/').substring(0,entryName.length()-6);
                classes.add(Class.forName(classBinaryName));
            }
        }
        return classes;
    }

    /**
     * 查看了好几个项目，发现都是读取xml文件来实现扫描项目路径下的class文件。这里通过递归解析url中的文件达到加载class的目的
     * @param url
     * @return
     */
    private static List<Class<?>> getClassesFromFile(URL url,String packageName) throws ClassNotFoundException, MalformedURLException {
        String path = packageName.replace('.',File.separatorChar);
        List<Class<?>> classes = new ArrayList<>();
        File file = new File(url.getFile());
        File[] files = file.listFiles();

        for (File f: files) {
            if (f.getPath().endsWith(".class")){
                String classBinaryName = packageName+"."+f.getName();
                classBinaryName = classBinaryName.substring(0,classBinaryName.length()-6);
                classes.add(Class.forName(classBinaryName));
            } else {
                String path1 = path.replace('/', File.separatorChar);
                int i = f.getPath().lastIndexOf(path1);
                String packageName2 = f.getPath().substring(i).replace(File.separatorChar,'.');
                classes.addAll(getClassesFromFile(f.toURI().toURL(),packageName2));
            }
        }
        return classes;
    }
}
