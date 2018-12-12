package com.coral.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by xss on 2018/11/19.
 */
public class CustomTask extends DefaultTask {

    @TaskAction
    void output() {
        println("params1 is ${project.testPluginExt.params1}")
        println("params2 is ${project.testPluginExt.params2}")
        println("params3 is ${project.testPluginExt.params3}")
    }
}
