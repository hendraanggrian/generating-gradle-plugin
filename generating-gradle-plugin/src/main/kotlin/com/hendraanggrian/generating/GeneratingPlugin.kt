package com.hendraanggrian.generating

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * BuildConfig and R Gradle plugin for Java projects.
 *
 * @see <a href="https://github.com/hendraanggrian/generating-gradle-plugin">generating-gradle-plugin</a>
 */
class GeneratingPlugin : Plugin<Project> {
    companion object {
        const val GROUP = LifecycleBasePlugin.BUILD_GROUP
        const val TASK_GENERATE_BUILDCONFIG = "generateBuildConfig"
        const val TASK_GENERATE_R = "generateR"
    }

    override fun apply(project: Project) {
        require(project.pluginManager.hasPlugin("java") || project.pluginManager.hasPlugin("java-library")) {
            "Generating Plugin requires `java` or `java-library`."
        }
        val hasApplicationPlugin = project.pluginManager.hasPlugin("application")
        val mainSourceSet = project.extensions.getByName<SourceSetContainer>("sourceSets")["main"]

        val generateBuildConfig = project.tasks.register<GenerateBuildConfigTask>(TASK_GENERATE_BUILDCONFIG) {
            group = GROUP
            description = "Generate Android-like BuildConfig class."
            packageName.convention(project.group.toString())
            appName.convention(project.name)
            appVersion.convention(project.version.toString())
            groupId.convention(project.group.toString())
        }
        val generateR = project.tasks.register<GenerateRTask>(TASK_GENERATE_R) {
            group = GROUP
            description = "Generate Android-like R class."
            packageName.convention(project.group.toString())
            resourcesDirectory.convention(mainSourceSet.resources.srcDirs.lastOrNull()?.takeIf { it.exists() })
        }
        if (generateBuildConfig.get().isEnabled) {
            mainSourceSet.java.srcDir(generateBuildConfig.get().outputDirectory)
        }
        if (generateR.get().isEnabled) {
            mainSourceSet.java.srcDir(generateR.get().outputDirectory)
        }

        project.afterEvaluate {
            if (hasApplicationPlugin) {
                generateBuildConfig.get().appName
                    .convention(project.extensions.getByType<JavaApplication>().applicationName)
            }
        }
    }
}
