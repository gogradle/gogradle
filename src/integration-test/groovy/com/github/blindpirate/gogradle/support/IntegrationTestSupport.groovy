package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GolangPlugin
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.tooling.BuildAction
import org.gradle.tooling.BuildActionExecuter
import org.gradle.tooling.BuildController
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.ConfigurableLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

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
    String goBinPath

    String buildDotGradleBase = '''
buildscript {
    dependencies {
        classpath files("${classpath}".split(java.io.File.pathSeparator))
    }
}
apply plugin: 'com.github.blindpirate.gogradle'

golang {
    goExecutable = "${goBinPath}"
}

'''

    void baseSetUp() {
        prepareMockGoBin()
        IOUtils.touch(new File(getProjectRoot(), 'settings.gradle'))
    }

    void prepareMockGoBin() {
        String mockGoBinName = Os.getHostOs() == Os.WINDOWS ? 'go.bat' : 'go'
        String mockGoBinContent = Os.getHostOs() == Os.WINDOWS ? mockGoBat : mockGo
        IOUtils.write(getProjectRoot(), mockGoBinName, mockGoBinContent)
        IOUtils.chmodAddX(getProjectRoot().toPath().resolve(mockGoBinName))
        goBinPath = getProjectRoot().toPath().resolve(mockGoBinName).toString()
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
        return [
                //"--debug",
                "-PgoBinPath=${getGoBinPath()}",
                "-PpluginRootProject=${getMainClasspath()}",
                "-Pclasspath=${getClasspath()}"]
    }

    abstract File getProjectRoot()

    String getGoBinPath() {
        return goBinPath.replace('\\', '/')
    }

    String getClasspath() {
        return System.getProperty('java.class.path').replace('\\', '/')
    }

    String getMainClasspath() {
        String classFullName = GolangPlugin.name.replace('.', '/') + '.class'
        String classFullPath = getClass().getClassLoader().getResource(classFullName)
        // file:
        return classFullPath - classFullName - 'file:'
    }

}
