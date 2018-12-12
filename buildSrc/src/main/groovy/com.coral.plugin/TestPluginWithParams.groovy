package com.coral.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

//import org.gradle.util.Clock

/**
 * Created by xss on 2018/11/19.
 */
class TestPluginWithParams implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('testPluginExt', TestPluginExtension)

        project.extensions.create('coralline', PersonExtension)
        project.extensions.create('address', AddressExtension)

        project.task('testTaskWithParams', type: CustomTask)

        project.task('testTaskWithParams2') << {
            def address = project['address']

            print("name = " + project['coralline'].name)
            println(", province = " + address.province + ", city = " + address.city)
        }
    }
}
