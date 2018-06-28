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
        stdout = new File(getProject().getProjectDir(), "build/${now}.stdout")
        stderr = new File(getProject().getProjectDir(), "build/${now}.stderr")
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
        def dirs = new File(path).listFiles().findAll { it.isDirectory() }
        Collections.shuffle(dirs)
        dirs.each {
            buildOne(it.toPath())
        }
    }

    void buildOne(Path path) {
        // https://github.com/user/project.git -> github.com/user/project
        String packagePath = 'git config --get remote.origin.url'.execute([], path.toFile()).text[8..-6]
        String buildDotGradle = """
buildscript {
    dependencies {
        classpath files('${getProject().getProjectDir().absolutePath}/build/libs/gogradle-0.10-all.jar')
    }
}
apply plugin: 'com.github.blindpirate.gogradle'

golang {
    packagePath = "${packagePath}" // path of project to be built 
}

repositories {
    golang {
        root ~/appengine.*/
        emptyDir()
    }
    
    golang {
        root 'common'
        emptyDir()
    }
}
"""
        if (path.resolve('build.gradle.ext').toFile().exists()) {
            stdout.append('Found extend build.gradle\n')
            stderr.append('Found extend build.gradle\n')
            buildDotGradle += path.resolve('build.gradle.ext').toFile().text
        } else if (!path.toFile().list().any { it.endsWith('.go') && !it.endsWith('_test.go') }) {
            buildDotGradle += """
build {
    go 'build ${packagePath}/...'
}
"""
        }
        write(new File(path.toFile(), 'build.gradle'), buildDotGradle)
        write(new File(path.toFile(), 'settings.gradle'), '')

        Path gradleLink = path.resolve('gradle')
        Path gradlewLink = path.resolve('gradlew')

        Files.deleteIfExists(gradleLink)
        Files.deleteIfExists(gradlewLink)

        Files.createSymbolicLink(gradleLink, getProject().getProjectDir().toPath().resolve('gradle'))
        Files.createSymbolicLink(gradlewLink, getProject().getProjectDir().toPath().resolve('gradlew'))

        stdout.append("Start building ${path}\n")
        stderr.append("Start building ${path}\n")

        if (!goInit(path)) {
            return
        }

        build(path)
    }

    private boolean build(Path path) {
        ProcessBuilder pb = new ProcessBuilder().command('./gradlew', 'build', 'lock', '--stacktrace').directory(path.toFile())
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(stdout))
        pb.redirectError(ProcessBuilder.Redirect.appendTo(stderr))


        if (pb.start().waitFor() == 0) {
            stderr.append("Building ${path} succeed\n")
            stdout.append("Building ${path} succeed\n")
            return true
        } else {
            stderr.append("Building ${path} failed\n")
            stdout.append("Building ${path} failed\n")
            return false
        }
    }

    private boolean goInit(Path path) {
        ProcessBuilder pb = new ProcessBuilder().command('./gradlew', 'init', '--stacktrace').directory(path.toFile())
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(stdout))
        pb.redirectError(ProcessBuilder.Redirect.appendTo(stderr))


        if (pb.start().waitFor() == 0) {
            stderr.append("Initializing ${path} succeed\n")
            stdout.append("Initializing ${path} succeed\n")
            return true
        } else {
            stderr.append("Initializing ${path} failed\n")
            stdout.append("Initializing ${path} failed\n")
            return false
        }
    }
}