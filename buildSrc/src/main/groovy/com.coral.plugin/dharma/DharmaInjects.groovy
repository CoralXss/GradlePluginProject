package com.coral.plugin.dharma

import com.coral.plugin.CallFinish
import javassist.ClassClassPath
import javassist.ClassPool
import javassist.CtBehavior
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.bytecode.CodeAttribute
import javassist.bytecode.LocalVariableAttribute
import javassist.bytecode.MethodInfo

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Created by xss on 2019/1/9.
 */
public class DharmaInjects {

    private static ClassPool classPool = ClassPool.getDefault()

    private static Set<String> set = new HashSet<>();

    public static void processDirClass(String path) {
        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse {
                collectClass(it.absolutePath)
            }
        }

        injectClassCode(path)
    }

    public static void processJarClass(File jarFile) {
        if (jarFile) {
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                collectClass(jarEntry.name)
            }
        }
    }

    private static boolean excludeClasses(String fullPathName) {
        return fullPathName.endsWith(".class") &&
                (!fullPathName.contains("android/support") &&
                !fullPathName.contains("android/arch") &&
                !fullPathName.contains('R$') &&
                !fullPathName.contains('R.class') &&
                !fullPathName.contains('BuildConfig.class'))
    }

    /**
     * 统计所有模块或jar包生成的 Dharma_Mapping_Manager_ 类
     * @param fullPathName
     */
    private static void collectClass(String fullPathName) {

        if (excludeClasses(fullPathName)) {
            println(fullPathName)

            if (fullPathName.contains("Dharma_Mapping_Manager_")) {
                int start = fullPathName.indexOf("Dharma_Mapping_Manager_")
                int end = fullPathName.length() - ".class".length()
                String className = fullPathName.substring(start, end)
                        .replace('\\', '.')
                        .replace('/', '.')
                println("className = " + className)
                set.add(className)
            }
        }
    }

    private static String insertString =
            "System.out.println(%s);"

    public static void injectClassCode(String path) {
        classPool.appendClassPath(path)

        set.each {
            CtClass ctClass = classPool.get("b.$it")

            // 解冻
            if (ctClass.isFrozen()) {
                ctClass.defrost()
            }

            CtField[] fields = ctClass.getFields()
            for (CtField ctField: fields) {

                String value = ctField.constantValue

                String targetClassName = value.split(";")[0]
                String targetMethodName = value.split(";")[1]

                println(targetClassName + ", " + targetMethodName)

                if (targetClassName && targetMethodName) {
                    CtClass targetClass = classPool.get(targetClassName)
                    CtMethod ctMethod = targetClass.getDeclaredMethod(targetMethodName)

                    String insertBeforeStr = "// 注释\n"

                    int paramLength = ctMethod.parameterTypes.length
                    if (paramLength > 0) {
                        println("paramLength = " + paramLength)
                        MethodInfo methodInfo = ctMethod.getMethodInfo();
                        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
                        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
                                .getAttribute(LocalVariableAttribute.tag);

                        for (int i = 0; i < paramLength; i++) {
                            println(attr.variableName(i))
                        }
                        insertString = String.format(insertString, attr.variableName(0))
                        insertBeforeStr = insertString
                        println(insertString)
                    }

                    ctMethod.insertBefore(insertBeforeStr)
                    targetClass.writeFile(path)
                    targetClass.detach()
                }
            }

            ctClass.detach()
        }
    }
}
