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

package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.go.GoCoverTest
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import org.gradle.tooling.BuildException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('go-test-cover')
class GoTestCoverIntegrationTest extends IntegrationTestSupport {

    String buildDotGradle

    @Before
    void setUp() {
        buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath='github.com/my/project'
}
"""
        writeBuildAndSettingsDotGradle(buildDotGradle)
        IOUtils.clearDirectory(new File(resource, 'src/'))
    }

    @Test
    @WithResource('')
    void 'setting env in test should succeed'() {
        // given
        IOUtils.write(resource, '1_test.go', '''
package main

import "testing"
import "os"

func Test_Env(t *testing.T){
    if "1" == os.Getenv("FOO") {
         t.Log("Passed")
    } else {
         t.Error("Failed")
    }
}
''')
        new File(resource, 'build.gradle').append('''
goTest {
    environment('FOO','1')
}
''')
        // when
        normalTest()
        // then
        assert stdout.toString().contains('1 completed, 0 failed')
    }

    @Test
    @WithResource('')
    void 'test can be skipped'() {
        // given
        IOUtils.write(resource, '1_test.go', '''
package main

import "testing"

func Test_Skip(t *testing.T){
    t.SkipNow()
}

func Test_Fail(t *testing.T){
    t.Error("Failed")
}

func Test_Pass(t *testing.T){
}
''')
        // then
        try {
            newBuild('goTest', 'goCover', '--info')
        } catch (BuildException e) {
            assert stdout.toString().contains('3 completed, 1 failed, 1 skipped')
        }
    }

    @Test
    void 'test and coverage report should be generated successfully'() {
        normalTest()
        assertNormalTestNoUpToDateResult()
        assertTestResults(13, 0, 0)
        GoCoverTest.examineCoverageHtmls(resource)

        normalTest()
        assertNormalTestUpToDateResult()

        // append at head to change the coverage profile
        IOUtils.write(resource, 'a/a1.go', '\n\n\n' + new File(resource, 'a/a1.go').text)
        normalTest()
        assertNormalTestNoUpToDateResult()

        IOUtils.forceDelete(new File(resource, '.gogradle/reports/test/index.html'))
        IOUtils.forceDelete(new File(resource, '.gogradle/reports/coverage/index.html'))
        normalTest()
        assertNormalTestNoUpToDateResult()
    }

    def normalTest() {
        newBuild('goTest', 'goCover', '--info')
    }

    def assertNormalTestNoUpToDateResult() {
        assert stdout.toString().contains('1 completed, 0 failed')
        assert stdout.toString().contains('3 completed, 0 failed')
        assert stdout.toString().contains('BUILD SUCCESSFUL')
        assert !stdout.toString().contains(':goTest UP-TO-DATE')
        assert !stdout.toString().contains(':goCover UP-TO-DATE')
        assert stdout.toString().contains('=== RUN   Test_B1_1')
        assert stdout.toString().contains('Coverage of package github.com/my/project/a:')
        assert stdout.toString().contains('Coverage of package github.com/my/project/b:')
        assert stdout.toString().contains('Total coverage:')

    }

    def assertNormalTestUpToDateResult() {
        assert !stdout.toString().contains('1 completed, 0 failed')
        assert stdout.toString().contains(':goTest UP-TO-DATE')
        assert stdout.toString().contains(':goCover UP-TO-DATE')
        assert stdout.toString().contains('BUILD SUCCESSFUL')
        assert !stdout.toString().contains('=== RUN   Test_B1_1')
    }

//    Caused by: org.gradle.tooling.internal.protocol.exceptions.InternalUnsupportedBuildArgumentException: Problem with provided build arguments: [--stacktrace, --tests a1_test.go].
//    Unknown command-line option '--tests a1_test.go'.
//    Either it is not a valid build option or it is not supported in the target Gradle version.
//    Not all of the Gradle command line options are supported build arguments.
//    Examples of supported build arguments: '--info', '-u', '-p'.
//    Examples of unsupported build options: '--daemon', '-?', '-v'.
//    Please find more information in the javadoc for the BuildLauncher class.
//    at org.gradle.tooling.internal.provider.ProviderStartParameterConverter.toStartParameter(ProviderStartParameterConverter.java:79)
    @Test
    void 'test with --tests should succeed'() {
        def result = runTestsWithPattern(['--tests', 'a1_test.go'])
        assertTestPatternNoUpToDateResult(result)

        result = runTestsWithPattern(['--tests', 'a1_test.go'])
        assertTestPatternUpToDateResult(result)

        result = runTestsWithPattern(['--tests', 'a1_test.go', '--tests', 'whatever'])
        assertTestPatternNoUpToDateResult(result)
    }

    def assertTestPatternNoUpToDateResult(def stdout) {
        assert stdout.contains('Found 1 files to test')
        assert stdout.contains('2 completed, 0 failed')
        assert !stdout.contains('3 completed, 0 failed')
        assert !stdout.contains(':goTest UP-TO-DATE')
        assert stdout.contains('BUILD SUCCESSFUL')
    }

    def assertTestPatternUpToDateResult(def stdout) {
        assert !stdout.contains('Found 1 files to test')
        assert !stdout.contains('2 completed, 0 failed')
        assert stdout.contains(':goTest UP-TO-DATE')
        assert stdout.contains('BUILD SUCCESSFUL')
    }

    def runTestsWithPattern(List args) {
        assert System.getProperty('GRADLE_DIST_HOME')
        String gradleBinPath = new File("${StringUtils.toUnixString(System.getProperty('GRADLE_DIST_HOME'))}/bin/gradle")
        if (Os.getHostOs() == Os.WINDOWS) {
            gradleBinPath += '.bat'
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream()

        CommandLine cmd = new CommandLine(gradleBinPath)
        cmd.addArguments('goTest', '-s', '--info')
        cmd.addArguments(args as String[])
        DefaultExecutor executor = new DefaultExecutor()
        executor.setWorkingDirectory(getProjectRoot())
        executor.setStreamHandler(new PumpStreamHandler(out))
        executor.execute(cmd)

        return out.toString()
    }


    @Test
    void 'test report should be generated if test fails'() {
        new File(resource, 'a/a1.go').delete()
        new File(resource, 'a/a1.go.fail').renameTo(new File(resource, 'a/a1.go'))
        new File(resource, 'b/b1.go').delete()
        new File(resource, 'b/b1.go.fail').renameTo(new File(resource, 'b/b1.go'))
        new File(resource, 'c/c1.go.fail').renameTo(new File(resource, 'c/c1.go'))
        new File(resource, 'c/c1_test.go.fail').renameTo(new File(resource, 'c/c1_test.go'))
        new File(resource, 'd/d1.go.fail').renameTo(new File(resource, 'd/d1.go'))
        new File(resource, 'd/d1_test.go.fail').renameTo(new File(resource, 'd/d1_test.go'))
        new File(resource, 'e/e_test.go').delete()
        new File(resource, 'e/e_test.go.fail').renameTo(new File(resource, 'e/e_test.go'))
        new File(resource, 'f/f_test.go').delete()
        new File(resource, 'f/f_test.go.fail').renameTo(new File(resource, 'f/f_test.go'))

        try {
            newBuild('goTest', 'goCover', '--info')
        } catch (BuildException e) {
            assertOutputContains('There are 9 failed tests')
            assertOutputContains('FAIL: Test_A1_1')
            examineTestReportHtmls()
        }
    }

    void examineTestReportHtmls() {
        assertTestResults(16, 9, 0)

        verifyTestHtml('.gogradle/reports/test/packages/github_DOT_com.my.project.a.html', 3, 1)
        verifyTestHtml('.gogradle/reports/test/classes/github_DOT_com.my.project.a.a1_test_DOT_go.html', 2, 1)
        verifyTestHtml('.gogradle/reports/test/classes/github_DOT_com.my.project.e.e_test_DOT_go.html', 5, 2)

        ['a', 'b', 'c', 'd', 'e'].each {
            assert new File(resource, ".gogradle/reports/test/packages/github_DOT_com.my.project.${it}.html").exists()
        }

        ['a1', 'a2'].each {
            assert new File(resource, ".gogradle/reports/test/classes/github_DOT_com.my.project.a.${it}_test_DOT_go.html").exists()
        }
    }

    void assertTestResults(int total, int failure, int ignore) {
        String indexHtml = new File(resource, '.gogradle/reports/test/index.html').text
        Document index = Jsoup.parse(indexHtml)
        // our rewrite script
        assert indexHtml.contains('h1,li>a,td>a,.breadcrumbs>a')
        assert index.select('#tests>.counter')[0].text().toInteger() == total
        assert index.select('#failures>.counter')[0].text().toInteger() == failure
        assert index.select('#ignored>.counter')[0].text().toInteger() == ignore
    }

    void verifyTestHtml(String file, int expectedTestCount, int expectedFailureCount) {
        String html = IOUtils.toString(new File(resource, file))
        Document doc = Jsoup.parse(html)
        assert html.contains('h1,li>a,td>a,.breadcrumbs>a')
        assert doc.select('#tests>.counter')[0].text().toInteger() == expectedTestCount
        assert doc.select('#failures>.counter')[0].text().toInteger() == expectedFailureCount
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
