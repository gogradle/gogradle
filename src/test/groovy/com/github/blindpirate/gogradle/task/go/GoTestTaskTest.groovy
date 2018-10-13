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

package com.github.blindpirate.gogradle.task.go

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.TaskTest
import com.github.blindpirate.gogradle.task.go.test.GoTestResultExtractor
import com.github.blindpirate.gogradle.util.ExceptionHandler
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult
import org.gradle.api.tasks.testing.TestResult
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.util.function.Consumer

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.VENDOR_TASK_NAME
import static com.github.blindpirate.gogradle.task.go.test.AbstractGoTestResultExtractor.GoTestMethodResult
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class GoTestTaskTest extends TaskTest {
    GoTest task

    File resource

    @Captor
    ArgumentCaptor<List> argumentsCaptor

    @Mock
    GoTestResultExtractor extractor

    @Before
    void setUp() {
        task = buildTask(GoTest)
        when(project.getProjectDir()).thenReturn(resource)
        when(setting.getPackagePath()).thenReturn('github.com/my/package')
        when(extractor.testParams()).thenReturn(['test', '-v'])

        ReflectionUtils.setField(task, 'extractor', extractor)

        IOUtils.write(resource, 'a/a1_test.go', '')
        IOUtils.write(resource, 'a/_a1_test.go', '')
        IOUtils.write(resource, 'a/a2_test.go', '')
        IOUtils.write(resource, 'a/.a2_test.go', '')
        IOUtils.write(resource, 'a/a1.go', '')
        IOUtils.write(resource, 'a/a2.go', '')

        IOUtils.write(resource, 'b/b1_test.go', '')
        IOUtils.write(resource, 'b/b2_test.go', '')
        IOUtils.write(resource, 'b/b1.go', '')
        IOUtils.write(resource, 'b/b2.go', '')

        IOUtils.write(resource, 'c/testdata/c_test.go', '')
        IOUtils.write(resource, 'c/c.go', '')
        IOUtils.write(resource, 'c/c.nongo', '')

        IOUtils.mkdir(resource, 'd')
        IOUtils.mkdir(resource, 'vendor')
        IOUtils.write(resource, 'vendor/vendor.go', '')
        IOUtils.write(resource, 'vendor/vendor_test.go', '')
    }

    @Test
    void 'test task should depend on install task'() {
        assertTaskDependsOn(task, VENDOR_TASK_NAME)
    }

    @Test
    void 'all package should be tested if not specified'() {
        // given
        task.environment('a', '1')
        task.environment('b', '2')
        // when
        task.run()
        // then
        verify(buildManager, times(2)).go(argumentsCaptor.capture(), eq([a: '1', b: '2']), any(Consumer), any(Consumer), eq(true))
        assert argumentsCaptor.getAllValues().contains(
                ['test', '-v', 'github.com/my/package/a',
                 "-coverprofile=${StringUtils.toUnixString(resource)}/.gogradle/reports/coverage/profiles/github.com%2Fmy%2Fpackage%2Fa.out".toString()])
        assert argumentsCaptor.getAllValues().contains(
                ['test', '-v', 'github.com/my/package/b',
                 "-coverprofile=${StringUtils.toUnixString(resource)}/.gogradle/reports/coverage/profiles/github.com%2Fmy%2Fpackage%2Fb.out".toString()])
    }

    @Test
    void 'nothing should happened if no tests match the specific pattern'() {
        // when
        ReflectionUtils.setField(task, 'testNamePattern', ["THIS WON'T BE MATCHED"])
        task.run()
        // then
        verifyNoMoreInteractions(buildManager)
    }

    @Test
    void 'coverage profiles should not be generated if not specified'() {
        // when
        task.setGenerateCoverageProfile(false)
        task.run()
        // then
        verify(buildManager, times(2)).go(argumentsCaptor.capture(), anyMap(), any(Consumer), any(Consumer), eq(true))
        assert argumentsCaptor.getAllValues().contains(['test', '-v', 'github.com/my/package/a'])
        assert argumentsCaptor.getAllValues().contains(['test', '-v', 'github.com/my/package/b'])
    }

    @Test
    void 'specific package should be tested if pattern is set'() {
        // when
        ReflectionUtils.setField(task, 'testNamePattern', ['a1*'])
        task.run()
        // then
        verify(buildManager).go(argumentsCaptor.capture(), anyMap(), any(Consumer), any(Consumer), eq(true))
        List<String> args = argumentsCaptor.getValue()
        assert args.size() == 6
        assert args[0..1] == ['test', '-v']
        assert ['a/a1_test.go', 'a/a1.go', 'a/a2.go'].any {
            args.contains(StringUtils.toUnixString(new File(resource, it)))
        }
    }

    @Test
    void 'collecting stdout should succeed'() {
        // given
        ReflectionUtils.setField(task, 'testNamePattern', ['a1*'])
        when(buildManager.go(anyList(), anyMap(), any(Consumer), any(Consumer), eq(true))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                invocation.arguments[2].accept('stdout')
                invocation.arguments[3].accept('stderr')
            }
        })
        ArgumentCaptor<PackageTestResult> argumentCaptor = ArgumentCaptor.forClass(PackageTestResult)
        // when
        task.run()
        // then
        verify(extractor).extractTestResult(argumentCaptor.capture())
        PackageTestResult context = argumentCaptor.getValue()
        assert context.packagePath == 'github.com/my/package/a'
        assert context.stdout == ['stdout', 'stderr']
        assert context.testFiles.size() == 3
    }

    @Test
    void 'successful and failed result should be counted correctly'() {
        // given
        TestClassResult result = new TestClassResult(1L, 'className', 2L)
        result.add(new GoTestMethodResult(1L, 'methodName', TestResult.ResultType.FAILURE, 2L, 3L, ''))
        result.add(new GoTestMethodResult(1L, 'methodName', TestResult.ResultType.SUCCESS, 2L, 3L, ''))
        when(extractor.extractTestResult(any(PackageTestResult))).thenReturn([result])

        ReflectionUtils.setField(task, 'testNamePattern', ['a1*'])

        // when
        try {
            task.run()
            assert false
        } catch (Exception e) {
            println(ExceptionHandler.getStackTrace(e))
            assert e.getMessage().contains('There are 1 failed tests')
        }
    }

    @Test
    void 'nothing should happened if no matched tests found'() {
        task.setTestNamePattern(['unexistent'])
        task.run()
    }

    @Test
    void 'rewriting html reports should succeed'() {
        // given
        String html = '<body></body>'
        List files = ['index.html', 'sub/index.html', 'sub/sub/index.html', 'sub/sub/index.nohtml']
        files.each { IOUtils.write(resource, it, html) }
        // when
        task.run()
        task.rewritePackageName(resource)
        // then
        files[0..2].each { assert new File(resource, it).text.contains('<script>') }
        assert new File(resource, 'sub/sub/index.nohtml').text == html
    }

    @Test
    void 'getting report directory should succeed'() {
        task.reportDir == new File(resource, '.gogradle/reports/test')
    }

    @Test
    void 'getting test name pattern should succeed'() {
        task.setTestNamePattern(['pattern'])
        assert task.testNamePattern == ['pattern']
    }

    @Test
    void 'getting all test go files should succeed'() {
        assert task.getAllGoFiles()*.name as Set == ['a1_test.go', 'a2_test.go', 'a1.go', 'a2.go', 'b1_test.go', 'b2_test.go', 'b1.go', 'b2.go', 'c.go', 'vendor.go'] as Set
    }

    @Test
    void 'getting go version should succeed'() {
        // when
        task.getGoVersion()
        // then
        verify(goBinaryManager).getGoVersion()
    }

    @Test
    void 'getting build tags should succeed'() {
        // when
        task.getBuildTags()
        // then
        verify(setting).getBuildTags()
    }
}
