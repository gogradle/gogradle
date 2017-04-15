package com.github.blindpirate.gogradle.task.go

import com.github.blindpirate.gogradle.GogradleRunner
import org.gradle.api.Action
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult
import org.gradle.api.tasks.testing.TestOutputEvent
import org.gradle.api.tasks.testing.TestResult
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.task.go.GoTestStdoutExtractor.GoTestMethodResult
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class GoTestResultsProviderTest {

    GoTestResultsProvider provider

    List<TestClassResult> classResults

    @Mock
    Writer writer

    @Before
    void setUp() {
        TestClassResult classResult1 = new TestClassResult(1L, 'className', 2L)
        GoTestMethodResult methodResult11 = new GoTestMethodResult(3L, 'methodName1', TestResult.ResultType.SUCCESS, 4L, 5L, '')
        GoTestMethodResult methodResult12 = new GoTestMethodResult(6L, 'methodName2', TestResult.ResultType.FAILURE, 6L, 7L, 'failure message')
        classResult1.add(methodResult11)
        classResult1.add(methodResult12)

        TestClassResult classResult2 = new TestClassResult(11L, 'className2', 12L)
        classResult2.add(new GoTestMethodResult(13L, 'methodName3', TestResult.ResultType.SUCCESS, 14L, 15L, '  '))

        classResults = [classResult1, classResult2]
        provider = new GoTestResultsProvider(classResults)
    }

    @Test(expected = UnsupportedOperationException)
    void 'writeNonTestOutput should be unsupported'() {
        new GoTestResultsProvider([]).writeNonTestOutput(0L, null, null)
    }

    @Test(expected = UnsupportedOperationException)
    void 'writeTestOutput should be unsupported'() {
        new GoTestResultsProvider([]).writeTestOutput(0L, 0L, null, null)
    }

    @Test
    void 'write all output of class should succeed'() {
        // when
        provider.writeAllOutput(1L, TestOutputEvent.Destination.StdOut, writer)
        // then
        writer.write('failure message')
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if writer throws IOException'() {
        when(writer.write(anyString())).thenThrow(IOException)
        provider.writeAllOutput(1L, TestOutputEvent.Destination.StdOut, writer)
    }

    @Test
    void 'visitClasses should succeed'() {
        Action action = mock(Action)
        provider.visitClasses(action)
        verify(action).execute(classResults[0])
        verify(action).execute(classResults[1])
    }

    @Test
    void 'hasOutput should succeed'() {
        assert !provider.hasOutput(1L, TestOutputEvent.Destination.StdErr)
        assert !provider.hasOutput(11L, TestOutputEvent.Destination.StdErr)
        assert provider.hasOutput(1L, TestOutputEvent.Destination.StdOut)
        assert provider.hasOutput(11L, TestOutputEvent.Destination.StdOut)
    }

    @Test
    void 'isHasResults should succeed'() {
        assert provider.isHasResults()
        assert !new GoTestResultsProvider([]).isHasResults()
    }

    @Test
    void 'close should succeed'() {
        provider.close()
    }
}
