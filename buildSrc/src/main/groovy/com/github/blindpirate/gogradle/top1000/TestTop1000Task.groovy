package com.github.blindpirate.gogradle.top1000

import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.nio.file.Files
import java.nio.file.Path

class TestTop1000Task extends DefaultTask {
    private static final Logger LOGGER = Logging.getLogger(TestTop1000Task)

    File top1000Dir

    void doTest(String specificProject) {
        top1000Dir = new File(getProject().getRootDir(), 'build/top1000')
        FileUtils.forceMkdir(top1000Dir)
        GithubTopRankCrawler.cloneAllInto(top1000Dir, false)

        if (specificProject) {
            buildOne(specificProject)
        } else {
            top1000Dir.list().each(this.&buildOne)
        }
    }

    void buildOne(String dirName) {
        File currentDir = new File(top1000Dir, dirName)
        if (currentDir.isFile()) {
            return
        }

        String projectImportPath = dirName.replaceAll("_", "/")
        String gogradleJarPath = new File(getProject().getRootDir(), "build/libs/gogradle-0.0.1-SNAPSHOT-all.jar")
        String buildDotGradle = """
buildscript {
    dependencies {
        classpath files("${gogradleJarPath}")
    }
}
apply plugin: 'com.github.blindpirate.gogradle'

golang {
    packagePath="github.com/${projectImportPath}"
}

build.dependsOn test
"""
        new File(currentDir, 'build.gradle').write(buildDotGradle)
        new File(currentDir, 'settings.gradle').write('')

        Path gradleLink = currentDir.toPath().resolve('gradle')
        Path gradlewLink = currentDir.toPath().resolve('gradlew')

        Files.deleteIfExists(gradleLink)
        Files.deleteIfExists(gradlewLink)

        Files.createSymbolicLink(gradleLink, getProject().getRootDir().toPath().resolve('gradle'))
        Files.createSymbolicLink(gradlewLink, getProject().getRootDir().toPath().resolve('gradlew'))

        File output = new File(top1000Dir, 'stdout')

        ProcessBuilder pb = new ProcessBuilder().command('./gradlew', 'build', '--stacktrace').directory(currentDir)
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(output))
        pb.redirectError(ProcessBuilder.Redirect.appendTo(output))

        output.append("Start building ${projectImportPath}")

        if (pb.start().waitFor() == 0) {
            output.append("Building ${projectImportPath} succeed")
        } else {
            output.append("Building ${projectImportPath} failed")
        }
    }
}
