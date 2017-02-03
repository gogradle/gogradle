package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GolangPlugin
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.ConfigurableLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

import java.nio.file.Path

abstract class IntegrationTestSupport {

    File resource

    File userhome

    ByteArrayOutputStream stdout = new ByteArrayOutputStream()
    PrintStream stdoutPs = new PrintStream(stdout)
    ByteArrayOutputStream stderr = new ByteArrayOutputStream()
    PrintStream stderrPs = new PrintStream(stderr)

    String mockGo = '''\
#!/usr/bin/env sh
echo 'go version go1.7.1 darwin/amd64'
'''
    String mockGoBat = '''\
echo go version go1.7.1 windows/amd64
'''
    Path mockGoBinPath

    String buildDotGradleBase = '''
buildscript {
    dependencies {
        classpath files("${jarPath}")
        classpath files("${classpath}".split(java.io.File.pathSeparatorChar as String))
    }
}
apply plugin: 'com.github.blindpirate.gogradle'

golang {
    goExecutable = "${mockGoBinPath}"
}
'''

    void baseSetUp() {
        prepareMockGoBin()
        IOUtils.touch(new File(getProjectRoot(), 'settings.gradle'))
        if (userhome != null) {
            System.setProperty('gradle.user.home', userhome.absolutePath)
        }
    }

    void prepareMockGoBin() {
        String mockGoBinName = Os.getHostOs() == Os.WINDOWS ? 'go.bat' : 'go'
        String mockGoBinContent = Os.getHostOs() == Os.WINDOWS ? mockGoBat : mockGo
        IOUtils.write(getProjectRoot(), mockGoBinName, mockGoBinContent)
        mockGoBinPath = getProjectRoot().toPath().resolve(mockGoBinName)
        IOUtils.chmodAddX(mockGoBinPath)
    }

    BuildLauncher newBuild(Closure closure) {
        ProjectConnection connection = newProjectConnection()
        try {
            BuildLauncher build = newProjectConnection().newBuild()

            configure(build)

            closure(build)
            build.run()
        } finally {
            connection.close()
        }
    }

    void configure(ConfigurableLauncher build) {
        build.setStandardOutput(stdoutPs)
        build.setStandardError(stderrPs)
        build.withArguments(buildArguments() as String[])
    }

    ProjectConnection newProjectConnection() {
        GradleConnector connector = GradleConnector.newConnector()
                .forProjectDirectory(getProjectRoot())

        if (userhome != null) {
            connector.useGradleUserHomeDir(userhome)
        }

        if (System.getProperty('GRADLE_DIST_HOME') != null) {
            connector.useInstallation(new File(System.getProperty('GRADLE_DIST_HOME')))
        }
        return connector.connect()
    }

    List<String> buildArguments() {
        String jarPath = new File("build/libs/gradle-golang-plugin-0.0.1-SNAPSHOT.jar").absolutePath

        return [
                //"--debug",
                "-PmockGoBinPath=${mockGoBinPath}",
                "-PjarPath=${jarPath}",
                "-PpluginRootProject=${getMainClasspath()}",
                "-Pclasspath=${getClasspath()}"]
    }

    abstract File getProjectRoot()

    String getClasspath() {
        return System.getProperty('java.class.path')
    }

    String getMainClasspath() {
        String classFullName = GolangPlugin.name.replace('.', '/') + '.class'
        String classFullPath = getClass().getClassLoader().getResource(classFullName)
        // file:
        return classFullPath - classFullName - 'file:'
    }

}
