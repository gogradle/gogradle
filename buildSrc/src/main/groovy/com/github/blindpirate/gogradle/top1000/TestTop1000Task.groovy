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
    File stdout
    File stderr

    void doTest(String specificProject) {
        String now = new Date().format('yyyyMMdd-HHmmss')
        top1000Dir = new File(getProject().getRootDir(), 'build/top1000')
        stdout = new File(getProject().getRootDir(), "build/${now}.stdout")
        stderr = new File(getProject().getRootDir(), "build/${now}.stderr")
        FileUtils.forceMkdir(top1000Dir)
//        GithubTopRankCrawler.cloneAllInto(top1000Dir, false)

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

        stdout.append("Start building ${projectImportPath}\n")
        stderr.append("Start building ${projectImportPath}\n:")

        ProcessBuilder pb = new ProcessBuilder().command('./gradlew', 'build', '--stacktrace').directory(currentDir)
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(stdout))
        pb.redirectError(ProcessBuilder.Redirect.appendTo(stderr))


        if (pb.start().waitFor() == 0) {
            stderr.append("Building ${projectImportPath} succeed\n")
            stdout.append("Building ${projectImportPath} succeed\n")
        } else {
            stderr.append("Building ${projectImportPath} failed\n")
            stdout.append("Building ${projectImportPath} failed\n")
        }
    }
}
