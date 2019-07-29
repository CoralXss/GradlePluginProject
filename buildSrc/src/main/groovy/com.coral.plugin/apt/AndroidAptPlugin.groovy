package com.coral.plugin.apt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.tasks.compile.GroovyCompile

/**
 * Created by xss on 2018/12/13.
 */
class AndroidAptPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def variants = null

        if (project.plugins.findPlugin("com.android.application") || project.plugins.findPlugin("android") ||
                project.plugins.findPlugin("com.android.test")) {
            variants = "applicationVariants"
        } else if (project.plugins.findPlugin("com.android.library") || project.plugins.findPlugin("android-library")) {
            variants = "libraryVariants"
        } else {
            throw new ProjectConfigurationException("The android or android-library plugin must be applied to the project.", null)
        }

        println("==============>>")
        println("plugins: " + project.plugins)

        println("==============>>")
        println("configurations: " + project.configurations)

        def aptConfiguration = project.configurations.create('apt')
                .extendsFrom(project.configurations.compile, project.configurations.provided)
//        project.extensions.create("apt", AndroidAptExtension)
        project.afterEvaluate {
            project.android[variants].all { variant ->

                println("==============>>")
                println("variant: " + variant)
                if (aptConfiguration.empty) {
                    println("No apt dependencies for configuration ${aptConfiguration.name}")
                    return
                }

                def aptOutputDir = project.file(new File(project.buildDir, "generated/source/apt"))
                def aptOutput = new File(aptOutputDir, variant.dirName)
                println("==============>>")
                println("aptOutput: " + aptOutput)

                def javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile
                variant.addJavaSourceFolderToModel(aptOutput)

                def processorPath = (aptConfiguration + javaCompile.classpath).asPath
                def taskDependency = aptConfiguration.buildDependencies
                if (taskDependency) {
                    javaCompile.dependsOn += taskDependency
                }

                javaCompile.options.compilerArgs += ['-s', aptOutput]
                javaCompile.options.compilerArgs += ['-processorpath', processorPath]

                javaCompile.doFirst {
                    aptOutput.mkdirs()
                }

                def dependency = javaCompile.finalizeBy
                def dependencies = dependency.getDependencies(javaCompile)
                for (def dep: dependencies) {
                    if (dep instanceof GroovyCompile) {
                        if (dep.groovyOptions.hasProperty("javaAnnotationProcessing")) {
                            dep.options.compilerArgs += javaCompile.options.compilerArgs
                            dep.groovyOptions.javaAnnotationProcessing = true
                        }
                    }
                }
            }
        }
    }

    static void configureVariant(def project, def aptConfiguration, def aptExtension) {

    }
}
