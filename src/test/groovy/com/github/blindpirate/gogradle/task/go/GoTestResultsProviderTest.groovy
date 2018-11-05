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
import org.gradle.api.Action
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult
import org.gradle.api.tasks.testing.TestOutputEvent
import org.gradle.api.tasks.testing.TestResult
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.task.go.test.AbstractGoTestResultExtractor.GoTestMethodResult
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
