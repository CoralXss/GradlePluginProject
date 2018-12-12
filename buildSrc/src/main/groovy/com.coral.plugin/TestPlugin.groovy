package com.coral.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by xss on 2018/11/19.
 */
class TestPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.task('testTask') << {
            println("Hello gradle plugin")
        }
    }
}
