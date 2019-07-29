package com.coral.plugin.javassist

import com.coral.plugin.CallFinish
import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtMethod
import org.gradle.api.Project

/**
 * Created by xss on 2018/11/20.
 */
public class MyInject {

    private static ClassPool classPool = ClassPool.getDefault()
    private static String injectStr = "System.out.println(\"Hello, World\");"

    public static void injectDir(String path, String pkgName) {
        // 将当前路径加入类池，不然会找不到该类
        classPool.appendClassPath(path)

        println("pkgName = " + pkgName)

        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
//                println("filePath = " + filePath)
                // 确保当前文件是 class 文件，而不是系统自动生成的 class 文件
                if (filePath.endsWith(".class")
                        && !filePath.contains('R$')
                        && !filePath.contains('R.class')
                        && !filePath.contains('BuildConfig.class')) {
                    // 判断当前目录是否在我们的应用包中
                    int index = filePath.indexOf(pkgName)
                    boolean isInMyPackage = (index != -1)
                    println("inIn = " + isInMyPackage)
                    if (isInMyPackage) {
                        int end = filePath.length() - 6 // 6为 .class 后缀长度
                        // 替换类路径中的分隔符
                        String className = filePath.substring(index, end)
                                .replace('\\', '.')
                                .replace('/', '.')
                        println("className = " + className)
                        // 开始修改 class文件
                        CtClass c = classPool.getCtClass(className)
                        if (c.isFrozen()) {
                            c.defrost()
                        }

                        CtConstructor[] cts = c.getDeclaredConstructors()
                        if (cts == null || cts.length == 0) {
                            // 手动创建一个构造函数
                            CtConstructor constructor = new CtConstructor(new CtClass[0], c)
                            constructor.insertBeforeBody(injectStr)
                            c.addConstructor(constructor)
                        } else {
                            cts[0].insertBeforeBody(injectStr)
                        }
                        c.writeFile(path)
                        c.detach()
                    }
                }
            }
        }
    }

    public static void injectCallFinishAnnotationMethod(String path, Project project) {
        classPool.appendClassPath(path)
        classPool.appendClassPath(project.android.bootClasspath[0].toString())

        // 读取 Dharma_Mapping_Manager_ 生成类的 field，进行拆分，

        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                if (file.getName().contains("Dharma_Mapping_Manager_")) {
                    // 获取 MainActivity
                    CtClass ctClass = classPool.getCtClass("com.coral.demo.MainActivity")
                    println("ctClass = " + ctClass)

                    // 解冻
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }

                    CtMethod ctMethod0 = ctClass.getDeclaredMethod("test")
                    println("ctMethod = " + ctMethod0 + ", " + ctMethod0.hasAnnotation(CallFinish.class))

                    // 获取到 onCreate() 方法
                    CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate")
                    if (ctMethod.hasAnnotation(CallFinish.class)) {
                        ctMethod.getAnnotation(CallFinish.class)

                        String insertBeforeStr = """android.util.Log.e("--->", "Hello");"""

                        ctMethod.insertBefore(insertBeforeStr)
                        ctClass.writeFile(path)
                        ctClass.detach()
                    }

                }
            }
        }
    }


    public static void injectOnCreate(String path, Project project) {
        classPool.appendClassPath(path)
        classPool.appendClassPath(project.android.bootClasspath[0].toString())
        classPool.importPackage("android.os.Bundle")

        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                if (file.getName().equals("MainActivity.class")) {
                    // 获取 MainActivity
                    println(file.getPath())

                }
            }
        }

        CtClass ctClass = classPool.getCtClass("com.coral.demo.MainActivity")
        println("ctClass = " + ctClass)

        // 解冻
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }

        CtMethod ctMethod0 = ctClass.getDeclaredMethod("test")
        println("ctMethod = " + ctMethod0 + ", " + ctMethod0.hasAnnotation(CallFinish.class))

        // 获取到 onCreate() 方法
        CtMethod ctMethod = ctClass.getDeclaredMethod("test")
        if (ctMethod.hasAnnotation(CallFinish.class)) {
            ctMethod.getAnnotation(CallFinish.class);

            String insertBeforeStr = """android.util.Log.e("--->", "Hello");"""

            ctMethod.insertBefore(insertBeforeStr)
            ctClass.writeFile(path)
            ctClass.detach()
        }
    }
}
