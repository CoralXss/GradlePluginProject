package com.coral.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Transform
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.coral.plugin.javassist.MyInject
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project


/**
 * Created by xss on 2018/11/19.
 */
public class MyTransform extends Transform {

    Project project

    /**
     * 构造方法，保留原project备用
     */
    MyTransform(Project project) {
        this.project = project
    }

    /**
     * 设置自定义 Transform 对应的 Task 名称
     * 类似：TransformClassesWithPreDexForXXX，对应的 task 名称为：transformClassesWithMyTransformForDebug
     * 会生成目录 build/intermediates/transforms/MyTransform/
     */
    @Override
    String getName() {
        return "MyTransform"
    }

    /**
     * 指定输入的类型，可指定我们要处理的文件类型（保证其他类型文件不会传入）
     * CLASSES - 表示处理java的class文件
     * RESOURCES - 表示处理java的资源
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指定 Transform 的作用范围
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 是否支持增量编译
     */
    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * 核心方法，具体如何处理输入和输出
     * @param inputs          为传过来的输入流，两种格式，一种jar包格式，一种目录格式
     * @param outputProvider  获取到输出目录，最后将修改的文件复制到输出目录，这一步必须执行，不让编译会报错
     */
    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {

        System.out.println("----------开始Transform-----------")
        // Transform 的 inputs 分为两种类型，一直是目录，一种是 jar 包。需要分开遍历

        inputs.each { TransformInput input ->
            // 1) 对类型为"目录"的 input 进行遍历
            input.directoryInputs.each { DirectoryInput dirInput ->
                // demo1. 在MainActivity的onCreate()方法之前注入代码
//                MyInject.injectOnCreate(dirInput.file.absolutePath, project)

                // demo2. 在构造方法时新增代码（文件夹中包含我们手写的类以及 R.class，BuildConfigClass 和 R$Xxx.class 等）
                MyInject.injectDir(dirInput.file.absolutePath, "com/coral/demo")

                // 获取 output 目录
                def dest = outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes,
                    dirInput.scopes, Format.DIRECTORY)
                // 将 input 的目录复制到 output 指定目录
                FileUtils.copyDirectory(dirInput.file, dest)
            }

            // 2) 对类型为 jar 文件的 input 进行遍历
            input.jarInputs.each { JarInput jarInput ->
                // jar 文件一般是第三方依赖库jar包

                // 重命名输出文件（同目录 copyFile 会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())

                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                // 生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes,
                    jarInput.scopes, Format.JAR)
                // 将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
            }
        }

        System.out.println("----------结束Transform-----------")
    }
}
