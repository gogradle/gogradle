package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Before


class IntegrationTestSupport {

    File resource

    File userhome

    String buildDotGradleBase = '''
buildscript {
    dependencies {
        classpath files("${jarPath}")
        classpath files("${classpath}".split(java.io.File.pathSeparatorChar as String))
    }
}
apply plugin: 'com.github.blindpirate.gogradle'
'''

    @Before
    void baseSetUp() {
        IOUtils.touch(resource.toPath().resolve('settings.gradle').toFile())
        System.setProperty('gradle.user.home', userhome.absolutePath)
        //IOUtils.write(userhome, 'gradle.properties', gradleDotProperties)
    }

    BuildLauncher newBuild(Closure closure) {
        GradleConnector connector = GradleConnector.newConnector()
                .forProjectDirectory(resource)
                .useGradleUserHomeDir(userhome)

        // That's not appropriate since it's not affected by gradle wrapper
        connector.useInstallation(new File(System.getProperty('GRADLE_DIST_HOME')))

        ProjectConnection connection = connector.connect()
        try {
            BuildLauncher build = connection.newBuild()

            build.setStandardOutput(System.out)
            build.setStandardError(System.err)

            String jarPath = new File("build/libs/gradle-golang-plugin-0.0.1-SNAPSHOT.jar").absolutePath

            build.withArguments(
                    //"--debug",
                    "-PjarPath=${jarPath}",
                    "-PpluginRootProject=${getMainClasspath()}",
                    "-Pclasspath=${getClasspath()}")

            closure(build)
            build.run()
        } finally {
            connection.close()
        }
    }

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
