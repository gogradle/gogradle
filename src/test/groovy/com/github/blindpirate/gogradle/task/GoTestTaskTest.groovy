package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.go.GoTestStdoutExtractor
import com.github.blindpirate.gogradle.task.go.GoTestTask
import com.github.blindpirate.gogradle.task.go.PackageTestContext
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult
import org.gradle.api.internal.tasks.testing.junit.result.TestMethodResult
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.testing.TestResult
import org.gradle.internal.operations.BuildOperationProcessor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.util.function.Consumer

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyList
import static org.mockito.ArgumentMatchers.anyMap
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.ArgumentMatchers.isNull
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class GoTestTaskTest extends TaskTest {
    GoTestTask task

    File resource

    @Captor
    ArgumentCaptor<List> argumentsCaptor

    @Mock
    GoTestStdoutExtractor extractor

    @Mock
    BuildOperationProcessor buildOperationProcessor

    @Before
    void setUp() {
        task = buildTask(GoTestTask)
        when(project.getRootDir()).thenReturn(resource)
        when(setting.getPackagePath()).thenReturn('github.com/my/package')

        ReflectionUtils.setField(task, 'extractor', extractor)
        ReflectionUtils.setField(task, 'buildOperationProcessor', buildOperationProcessor)

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

        IOUtils.mkdir(resource, 'c/testdata/c_test.go')
        IOUtils.write(resource, 'c/c.go', '')
        IOUtils.write(resource, 'c/c.nongo', '')

        IOUtils.mkdir(resource, 'd')
        IOUtils.mkdir(resource, 'vendor')
    }

    @Test
    void 'test task should depend on install task'() {
        assertTaskDependsOn(task, INSTALL_TEST_DEPENDENCIES_TASK_NAME)
        assertTaskDependsOn(task, INSTALL_BUILD_DEPENDENCIES_TASK_NAME)
    }

    @Test
    void 'all package should be tested if not specified'() {
        // when
        task.addDefaultActionIfNoCustomActions()
        task.actions[0].execute(task)
        // then
        verify(buildManager, times(2)).go(argumentsCaptor.capture(), isNull(), any(Consumer), any(Consumer), any(Consumer))
        assert argumentsCaptor.getAllValues().contains(
                ['test', '-v', 'github.com/my/package/a',
                 "-coverprofile=${StringUtils.toUnixString(resource)}/.gogradle/coverage/profiles/github.com%2Fmy%2Fpackage%2Fa".toString()])
        assert argumentsCaptor.getAllValues().contains(
                ['test', '-v', 'github.com/my/package/b',
                 "-coverprofile=${StringUtils.toUnixString(resource)}/.gogradle/coverage/profiles/github.com%2Fmy%2Fpackage%2Fb".toString()])
    }

    @Test
    void 'coverage profiles should not be generated if not specified'() {
        // when
        task.addDefaultActionIfNoCustomActions()
        task.setGenerateCoverageProfile(false)
        task.actions[0].execute(task)
        // then
        assert !task.generateCoverageProfile
        verify(buildManager, times(2)).go(argumentsCaptor.capture(), isNull(), any(Consumer), any(Consumer), any(Consumer))
        assert argumentsCaptor.getAllValues().contains(['test', '-v', 'github.com/my/package/a'])
        assert argumentsCaptor.getAllValues().contains(['test', '-v', 'github.com/my/package/b'])
    }

    @Test
    void 'specific package should be tested if pattern is set'() {
        // when
        ReflectionUtils.setField(task, 'testNamePattern', ['a1*'])
        task.addDefaultActionIfNoCustomActions()
        task.actions[0].execute(task)
        // then
        verify(buildManager).go(argumentsCaptor.capture(), isNull(Map), any(Consumer), any(Consumer), any(Consumer))
        List<String> args = argumentsCaptor.getValue()
        assert args.size() == 6
        assert args[0..1] == ['test', '-v']
        assert ['a/a1_test.go', 'a/a1.go', 'a/a2.go'].any { args.contains(new File(resource, it).absolutePath) }
    }

    @Test
    void 'collecting stdout should succeed'() {
        // given
        ReflectionUtils.setField(task, 'testNamePattern', ['a1*'])
        when(buildManager.go(anyList(), isNull(Map), any(Consumer), any(Consumer), any(Consumer))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                invocation.arguments[2].accept('stdout')
                invocation.arguments[3].accept('stderr')
            }
        })
        ArgumentCaptor<PackageTestContext> argumentCaptor = ArgumentCaptor.forClass(PackageTestContext)
        // when
        task.addDefaultActionIfNoCustomActions()
        task.actions[0].execute(task)
        // then
        verify(extractor).extractTestResult(argumentCaptor.capture())
        PackageTestContext context = argumentCaptor.getValue()
        assert context.packagePath == 'github.com/my/package/a'
        assert context.stdout == 'stdout\nstderr\n'
        assert context.testFiles.size() == 3
    }

    @Test
    void 'successful and failed result should be counted correctly'() {
        // given
        TestClassResult result = new TestClassResult(1L, 'className', 2L)
        result.add(new TestMethodResult(1L, 'methodName', TestResult.ResultType.FAILURE, 2L, 3L))
        result.add(new TestMethodResult(1L, 'methodName', TestResult.ResultType.SUCCESS, 2L, 3L))
        when(extractor.extractTestResult(any(PackageTestContext))).thenReturn([result])

        Logger mockLogger = mock(Logger)
        ReflectionUtils.setStaticFinalField(GoTestTask, 'LOGGER', mockLogger)
        ReflectionUtils.setField(task, 'testNamePattern', ['a1*'])

        // when
        task.addDefaultActionIfNoCustomActions()
        task.actions[0].execute(task)
        // then
        verify(mockLogger).quiet("Test for {} finished, {} succeed, {} failed", 'github.com/my/package/a', 1, 1)
        ReflectionUtils.setStaticFinalField(GoTestTask, 'LOGGER', Logging.getLogger(GoTestTask))
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if go test return non-zero'() {
        // given
        when(buildManager.go(anyList(), isNull(Map), any(Consumer), any(Consumer), any(Consumer))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                invocation.arguments[4].accept(0)
                invocation.arguments[4].accept(1)
            }
        })
        // when
        task.addDefaultActionIfNoCustomActions()
        task.actions[0].execute(task)
    }

    @Test
    void 'nothing should happened if no matched tests found'() {
        // when
        task.setTestNamePattern(['unexistent'])
        task.addDefaultActionIfNoCustomActions()
        // then
        task.actions.size() == 0
    }
}
