package com.coral.plugin.dharma

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by xss on 2019/1/9.
 */
class DharmaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        def android = project.extensions.getByType(AppExtension)
        // 注册Transform
        def classTransform = new DharmaTransform(project)
        android.registerTransform(classTransform)

        // 安装apk时会触发这个 Transform 执行，其他时机不行。
        // 遗留问题：使用 Javassist 没有织入成功。
    }
}
