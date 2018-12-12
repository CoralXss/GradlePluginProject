package com.coral.plugin

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension

/**
 * Created by xss on 2018/11/20.
 * desc: 利用 Javassist，在系统自动生成BuildConfig.java文件后，自动生成我们的java文件
 */
public class CreateJavaPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        System.out.println("----------------Begin----------------")
        System.out.println("This is out custom plugin.")

        def android = project.extensions.getByType(AppExtension)

        // 注册一个Transform
        def classTransform = new MyTransform(project)
        android.registerTransform(classTransform)

        // 创建一个 Extension
        project.extensions.create("testCreateJavaConfig", CreateJavaExtension)

        // 生产一个类
        if (project.plugins.hasPlugin(AppPlugin)) {
            // 获取到 Extension，也即是 .gradle 文件中的闭包
            android.applicationVariants.all { variant ->
                // 获取到 scope 作用域
                def variantData = variant.variantData
                def scope = variantData.scope

                // 拿到 .gradle 中配置的 Extension 值
                def config = project.extensions.getByName("testCreateJavaConfig")

                // 创建一个 Task（名称为：coralDebugCreateJavaPlugin 或 coralReleaseCreateJavaPlugin）
                def createTaskName = scope.getTaskName("coral", "CreateJavaPlugin")
                def createTask = project.task(createTaskName)

                // 设置 task 要执行的任务
                createTask.doLast {
                    // 生成 java 类
                    createJavaTest(variant, config)
                }

                // 设置 task 依赖于生成 BuildConfig 的 task，然后在生成 BuildConfig 后生成我们的类
                String generateBuildConfigTaskName = variant.getVariantData()
                        .getScope().getGenerateBuildConfigTask().name
                // 任务名称：generateDebugBuildConfig
                println("generateBuildConfigTaskName = " + generateBuildConfigTaskName)

                def generateBuildConfigTask = project.tasks.getByName(generateBuildConfigTaskName)
                if (generateBuildConfigTask) {
                    createTask.dependsOn generateBuildConfigTask
                    generateBuildConfigTask.finalizedBy createTask
                }
            }
        }

        System.out.println("----------------Has it finished?----------------")
    }

    static void createJavaTest(variant, config) {
        println("---begin create: " + variant + ", " + config.str)
        // 要生成的内容
        def content = """package com.coral.demo;
/**
* Created by xss on 2018/11/20.
*/
public class TestClass {
    public static final String str = "${config.str}";
}
                      """
        // 获取到 BuildConfig 类的路径
        File outputDir = variant.getVariantData().getScope().getBuildConfigSourceOutputDir()
        // /Users/xss/Documents/AndroidStudioProjects/GradlePluginProject/app/build/generated/source/buildConfig/debug
        println("outputDir = " + outputDir.absolutePath)
        def javaFile = new File(outputDir, "TestClass.java")
        javaFile.write(content, 'UTF-8')
        println("---create finished---")
    }
}
