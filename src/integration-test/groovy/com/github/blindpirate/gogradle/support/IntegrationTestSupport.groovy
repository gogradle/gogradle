package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.util.IOUtils
import org.gradle.tooling.*

abstract class IntegrationTestSupport {
    File resource

    File userhome

    ByteArrayOutputStream stdout = new ByteArrayOutputStream()
    PrintStream stdoutPs = new PrintStream(stdout)
    ByteArrayOutputStream stderr = new ByteArrayOutputStream()
    PrintStream stderrPs = new PrintStream(stderr)

    // We use real go by default
    String goBinPath = ''

    String buildDotGradleBase = """ 
buildscript {
    dependencies {
        classpath files(new File(rootDir, '../../libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar'))
    }
}
apply plugin: 'com.github.blindpirate.gogradle'
""" + '''
golang {
    goExecutable = '${goBinPath}'
}
'''

    void writeBuildAndSettingsDotGradle(String buildDotGradle) {
        writeBuildAndSettingsDotGradle(buildDotGradle, '')
    }

    void writeBuildAndSettingsDotGradle(String buildDotGradle, String settingsDotGradle) {
        IOUtils.write(getProjectRoot(), 'settings.gradle', settingsDotGradle)
        IOUtils.write(getProjectRoot(), 'build.gradle', buildDotGradle)
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
        return ["-PgoBinPath=${getGoBinPath()}", "--stacktrace"]
    }

    abstract File getProjectRoot()

    String getGoBinPath() {
        return goBinPath.replace('\\', '/')
    }

}
