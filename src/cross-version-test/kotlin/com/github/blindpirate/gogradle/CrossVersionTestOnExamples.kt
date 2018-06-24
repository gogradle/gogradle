package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import com.github.blindpirate.gogradle.support.OnlyWhen
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(GogradleRunner::class)
@com.github.blindpirate.gogradle.support.OnlyWhen("System.getenv('EXAMPLES_DIR')!=null&&'git version'.execute()")
class CrossVersionTestOnExamples {
    val processUtils = ProcessUtils()
    var projectDir: File = File(System.getenv("EXAMPLES_DIR"))

    @Before
    fun setUp() {
        processUtils.runAndGetStdout(projectDir, "git", "reset", "HEAD", "--hard")
    }

    fun crossVersionTest(function: (String) -> Unit) {
        CrossVersionSmokeTest.VERSIONS.forEach {
            println("Start building with $it.")
            function(it)
        }
    }

    fun useDirectory(dir: String) {
        projectDir = File(System.getenv("EXAMPLES_DIR"), dir)
        println(projectDir)
    }

    fun replacePluginsWithCurrentGogradleJar() {
        var buildScriptLines = IOUtils.readLines(File(projectDir, "build.gradle"))
        buildScriptLines = buildScriptLines.subList(3, buildScriptLines.size)
        buildScriptLines = listOf("""
buildscript {
    dependencies {
        classpath files('${System.getProperty("GOGRADLE_ROOT")}/build/libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar')
    }
}
apply plugin: 'com.github.blindpirate.gogradle'
            """) + buildScriptLines
        File(projectDir, "build.gradle").writeText(buildScriptLines.joinToString("\n"))
    }

    fun replaceDependenciesWithCurrentGogradleJar() {
        var buildScriptLines = ArrayList(IOUtils.readLines(File(projectDir, "build.gradle")))
        var index = buildScriptLines.indexOfFirst { it.contains("gradle.plugin.com.github.blindpirate:gogradle:") }
        buildScriptLines[index] = "classpath files('${System.getProperty("GOGRADLE_ROOT")}/build/libs/gogradle-${GogradleGlobal.GOGRADLE_VERSION}-all.jar')"
        File(projectDir, "build.gradle").writeText(buildScriptLines.joinToString("\n"))
    }

    fun cleanProjectDir() {
        listOf(".gogradle", ".gradle", "vendor", "StringReverse", "output", "stdout", "stderr").forEach { prefix ->
            projectDir.listFiles().forEach { file ->
                if (file.name.startsWith(prefix)) {
                    IOUtils.deleteQuitely(file)
                }
            }
        }

        IOUtils.deleteQuitely(File(projectDir, "build"))
    }

    fun testOneExample(dir: String, task: String, assertion: () -> Unit) {
        useDirectory(dir)
        replacePluginsWithCurrentGogradleJar()

        replaceRelativePathToAbsolutePath()

        crossVersionTest {
            cleanProjectDir()
            GradleRunner.create()
                    .withProjectDir(projectDir)
                    .withArguments(listOf(task, "--info", "--stacktrace", "--no-daemon", "-Duser.dir=" + projectDir.absolutePath))
                    .withGradleVersion(it)
                    .forwardOutput()
                    .build()
            assertion()
        }
    }

    private fun replaceRelativePathToAbsolutePath() {
        var buildScriptLines = ArrayList(IOUtils.readLines(File(projectDir, "build.gradle")))
        buildScriptLines.forEachIndexed { index, s ->
            if (s.contains(".private/repo")) {
                buildScriptLines[index] = "dir '${projectDir.absolutePath.replace("\\", "/")}/.private/repo'"
            }
        }
        File(projectDir, "build.gradle").writeText(buildScriptLines.joinToString("\n"))
    }

    @Test
    fun testBuildSubpackageAndCrossCompile() {
        testOneExample("build-subpackage-and-cross-compile", "build") {
            listOf("StringReverse_darwin_amd64", "StringReverse_linux_386", "StringReverse_windows_amd64.exe").forEach {
                assert(File(projectDir, it).exists())
            }
        }
    }

    @Test
    @OnlyWhen(value = "'golint'.execute()", ignoreTestWhenException = OnlyWhen.ExceptionStrategy.TRUE)
    fun testGofmtVetGolint() {
        testOneExample("gofmt-vet-golint", "build") {
            assert(IOUtils.toString(File(projectDir, "stdout.txt")).contains("should not use dot imports"))
        }
    }

    @Test
    fun testTypicalBuild() {
        testOneExample("typical-build", "build") {
            assert(File(projectDir, "StringReverse${Os.getHostOs().exeExtension()}").exists())
            assert(processUtils.runAndGetStdout(File(projectDir, "StringReverse${Os.getHostOs().exeExtension()}").absolutePath) == "Hello, Go examples!\n")
        }
    }

    @Test
    fun testRepositoryManagement() {
        testOneExample("repository-management", "build") {
            assert(processUtils.runAndGetStdout(File(projectDir, "output${Os.getHostOs().exeExtension()}").absolutePath) == "I am a private repo\n")
        }
    }

    @Test
    fun testMultiProject() {
        useDirectory("multi-project")
        replaceDependenciesWithCurrentGogradleJar()

        crossVersionTest {
            cleanProjectDir()
            GradleRunner.create()
                    .withProjectDir(projectDir)
                    .withArguments(listOf("build", "--info"))
                    .withGradleVersion(it)
                    .forwardOutput()
                    .build()

            assert(processUtils.runAndGetStdout(File(projectDir, "build/libs/go1${Os.getHostOs().exeExtension()}").absolutePath).contains("I am go1"))
            assert(processUtils.runAndGetStdout(File(projectDir, "build/libs/go2${Os.getHostOs().exeExtension()}").absolutePath).contains("I am go2"))
            assert(File(projectDir, "build/libs/multi-project.jar").exists())
        }
    }

    @Test
    fun testFailedTest() {
        useDirectory("failed-test")
        replacePluginsWithCurrentGogradleJar()

        crossVersionTest {
            cleanProjectDir()
            GradleRunner.create()
                    .withProjectDir(projectDir)
                    .withArguments(listOf("test", "--info"))
                    .withGradleVersion(it)
                    .forwardOutput()
                    .buildAndFail()

            assert(IOUtils.toString(File(projectDir, ".gogradle/reports/test/index.html")).contains("33%"))
        }
    }

    @Test
    fun testHandleTaskNameConflict() {
        useDirectory("handle-task-name-conflict")
        crossVersionTest {
            cleanProjectDir()
            GradleRunner.create()
                    .withProjectDir(projectDir)
                    .withArguments(listOf("goBuild", "--info", "-Dgogradle.alias=true"))
                    .withGradleVersion(it)
                    .forwardOutput()
                    .build()
        }
    }
}