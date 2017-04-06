package com.github.blindpirate.gogradle

import org.gradle.api.DefaultTask

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

// You can clone Github's top 1000 Go repositories for test
// See https://github.com/blindpirate/report-of-build-tools-for-java-and-golang
class TestTop1000Task extends DefaultTask {
    File top1000Dir
    File stdout
    File stderr

    void init() {
        String now = new Date().format('yyyyMMdd-HHmmss')
        stdout = new File(getProject().getRootDir(), "build/${now}.stdout")
        stderr = new File(getProject().getRootDir(), "build/${now}.stderr")
    }

    void write(File file, String text) {
        if (!file.exists()) {
            file.createNewFile()
        }
        file.write(text)
    }

    void testOne(String path) {
        init()
        buildOne(Paths.get(path))
    }

    void testAll(String path) {
        init()
        new File(path).eachDir { buildOne(it.toPath()) }
    }

    void buildOne(Path path) {
        String dirName = path.getFileName()
        String[] userAndProject = dirName.split(/_/)
        String buildDotGradle = """
buildscript {
    dependencies {
        classpath files('${getProject().getRootDir().absolutePath}/build/libs/gogradle-0.4.1-all.jar')
    }
}
apply plugin: 'com.github.blindpirate.gogradle'

golang {
    packagePath = "github.com/${userAndProject[0]}/${userAndProject[1]}" // path of project to be built 
}
"""
        if (path.resolve('build.gradle.ext').toFile().exists()) {
            stdout.append('Found extend build.gradle\n')
            stderr.append('Found extend build.gradle\n')
            buildDotGradle += path.resolve('build.gradle.ext').toFile().text
        } else if (!path.toFile().list().any { it.endsWith('.go') }) {
            buildDotGradle += '''
build {
    doLast {
        go 'build ./...'
    }
}
'''
        }
        write(new File(path.toFile(), 'build.gradle'), buildDotGradle)
        write(new File(path.toFile(), 'settings.gradle'), '')

        Path gradleLink = path.resolve('gradle')
        Path gradlewLink = path.resolve('gradlew')

        Files.deleteIfExists(gradleLink)
        Files.deleteIfExists(gradlewLink)

        Files.createSymbolicLink(gradleLink, getProject().getRootDir().toPath().resolve('gradle'))
        Files.createSymbolicLink(gradlewLink, getProject().getRootDir().toPath().resolve('gradlew'))

        stdout.append("Start building ${path}\n")
        stderr.append("Start building ${path}\n")

        ProcessBuilder pb = new ProcessBuilder().command('./gradlew', 'clean', 'build', '--stacktrace').directory(path.toFile())
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(stdout))
        pb.redirectError(ProcessBuilder.Redirect.appendTo(stderr))


        if (pb.start().waitFor() == 0) {
            stderr.append("Building ${path} succeed\n")
            stdout.append("Building ${path} succeed\n")
        } else {
            stderr.append("Building ${path} failed\n")
            stdout.append("Building ${path} failed\n")
        }
    }
}