package com.coral.plugin

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension

/**
 * Created by xss on 2018/11/20.
 */
public class MyTransformPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        def android = project.extensions.getByType(AppExtension)
        // 注册Transform
        def classTransform = new MyTransform(project)
        android.registerTransform(classTransform)

        // 安装apk时会触发这个 Transform 执行，其他时机不行。
        // 遗留问题：使用 Javassist 没有织入成功。
    }
}
