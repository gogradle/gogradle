/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.gradle.tooling.*
import org.junit.Before

@SuppressFBWarnings('SE_BAD_FIELD_INNER_CLASS')
abstract class IntegrationTestSupport {
    File resource

    File userhome

    ByteArrayOutputStream stdout
    PrintStream stdoutPs
    ByteArrayOutputStream stderr
    PrintStream stderrPs

    // We use real go by default
    String goBinPath = 'go'

    String buildDotGradleBase

    @Before
    void baseSetUp() {
        buildDotGradleBase = """ 
buildscript {
    dependencies {
        classpath files('${System.getProperty('GOGRADLE_ROOT')}/build/libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar')
    }
}
apply plugin: 'com.github.blindpirate.gogradle'
golang {
    goExecutable = '${StringUtils.toUnixString(goBinPath)}'
    packagePath = 'github.com/my/project'
}
"""
        if (userhome != null) {
            buildDotGradleBase = "System.setProperty('gradle.user.home','${StringUtils.toUnixString(userhome)}')" + buildDotGradleBase
        }

    }

    void initStdoutStderr() {
        stdout = new ByteArrayOutputStream()
        stdoutPs = new PrintStream(stdout)
        stderr = new ByteArrayOutputStream()
        stderrPs = new PrintStream(stderr)
    }

    void writeBuildAndSettingsDotGradle(String buildDotGradle) {
        writeBuildAndSettingsDotGradle(buildDotGradle, '')
    }

    void writeBuildAndSettingsDotGradle(String buildDotGradle, String settingsDotGradle) {
        IOUtils.write(getProjectRoot(), 'settings.gradle', settingsDotGradle)
        IOUtils.write(getProjectRoot(), 'build.gradle', buildDotGradle)
    }


    BuildLauncher newBuild(Closure closure, List arguments) {
        initStdoutStderr()
        ProjectConnection connection = newProjectConnection()
        try {
            BuildLauncher build = newProjectConnection().newBuild()

            configure(build, arguments)

            closure(build)

            build.run()
        } catch (Exception e) {
            println("stdout:\n${stdout}")
            println("stderr:\n${stderr}")
            throw e
        } finally {
            connection.close()
        }
    }

    @Deprecated
    BuildLauncher newBuild(Closure closure) {
        newBuild(closure, [])
    }

    BuildLauncher newBuild(String... taskAndArgs) {
        newBuild(taskAndArgs.findAll({ !it.startsWith('-') }), taskAndArgs.findAll({ it.startsWith('-') }))
    }

    BuildLauncher newBuild(List tasks, List args) {
        newBuild({
            it.forTasks(tasks as String[])
        }, args)
    }

    Object buildAction(Closure closure) {
        ProjectConnection connection = newProjectConnection()
        try {
            BuildActionExecuter executor = connection.action(new BuildAction() {
                @Override
                Object execute(BuildController controller) {
                    closure(controller)
                }
            })
            return executor.run()
        } finally {
            connection.close()
        }
    }

    void configure(ConfigurableLauncher build, List arguments) {
        build.setStandardOutput(stdoutPs)
        build.setStandardError(stderrPs)
        build.withArguments(['--stacktrace'] + arguments)
    }

    ProjectConnection newProjectConnection() {
        GradleConnector connector = GradleConnector.newConnector()
                .forProjectDirectory(getProjectRoot())

        if (System.getProperty('GRADLE_DIST_HOME') != null) {
            connector.useInstallation(new File(System.getProperty('GRADLE_DIST_HOME')))
        }
        return connector.connect()
    }

    File getProjectRoot() {
        assert resource != null: "You should annotate the class with @WithResource !"
        return resource
    }

    String getGoBinPath() {
        return goBinPath.replace('\\', '/')
    }

    void init() {
        IOUtils.deleteQuitely(new File(getProjectRoot(), 'gogradle.lock'))
        try {
            newBuild {
                it.forTasks('init')
            }
        } finally {
            println(stdout)
            println(stderr)
        }
    }

    void setUpDebug() {
        IOUtils.write(getProjectRoot(), 'gradle.properties', 'org.gradle.jvmargs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005')
    }
}
