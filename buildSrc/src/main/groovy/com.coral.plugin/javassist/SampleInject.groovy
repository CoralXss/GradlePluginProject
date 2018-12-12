package com.coral.plugin.javassist

import javassist.ClassPool
import javassist.CtClass;

/**
 * Created by xss on 2018/11/21.
 */
public class SampleInject {

    private static ClassPool classPool = ClassPool.getDefault()

    public static void injectSuperClass() {
        CtClass ctClass = classPool.getCtClass("")

    }
}
