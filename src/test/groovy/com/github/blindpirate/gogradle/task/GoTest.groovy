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

package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.Go
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.util.function.Consumer

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class GoTest extends TaskTest {

    Go task

    List stdouts = ['stdout1', 'stdout2']

    List stderrs = ['stderr1', 'stderr2']

    File resource

    @Captor
    ArgumentCaptor<List> captor

    @Before
    void setUp() {
        task = buildTask(Go)
        when(project.getProjectDir()).thenReturn(resource)
    }

    void letBuildManagerCallConsumer() {
        Answer answer = new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                Consumer stdoutLineConsumer = invocation.getArgument(2)
                Consumer stderrLineConsumer = invocation.getArgument(3)

                stdouts.each { stdoutLineConsumer.accept(it) }
                stderrs.each { stderrLineConsumer.accept(it) }

                return 0
            }
        }
        when(buildManager.run(anyList(), anyMap(), any(Consumer), any(Consumer), anyBoolean())).thenAnswer(answer)
        when(buildManager.go(anyList(), anyMap(), any(Consumer), any(Consumer), anyBoolean())).thenAnswer(answer)
    }

    @Test
    void 'go command should succeed'() {
        // when
        task.go('build -o "output name"')
        task.executeTask()
        // then
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List)
        verify(buildManager).go(captor.capture(), anyMap(), any(Consumer), any(Consumer), eq(false))
        assert captor.getValue() == ['build', '-o', 'output name']
    }

    @Test(expected = MissingMethodException)
    void 'exception should be thrown if args number of go is more than 1'() {
        task.go('a', 'b')
    }

    @Test
    void 'run command should succeed'() {
        // when
        task.run('golint -v -a')
        task.executeTask()
        // then
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List)
        verify(buildManager).run(captor.capture(), anyMap(), any(Consumer), any(Consumer), eq(false))
        assert captor.getValue() == ['golint', '-v', '-a']
    }

    @Test(expected = MissingMethodException)
    void 'exception should be thrown if args number of run is more than 1'() {
        task.run('a', 'b')
    }

    @Test
    void 'setting continueOnFailure should succeed'() {
        // given
        task.continueOnFailure = true
        // when
        task.go('test -v github.com/my/package')
        task.executeTask()
        // then
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List)
        verify(buildManager).go(captor.capture(), anyMap(), any(Consumer), any(Consumer), eq(true))
        assert captor.value == ['test', '-v', 'github.com/my/package']
    }

    @Test
    void 'consuming stdout and stderr in go should succeed'() {
        // given
        letBuildManagerCallConsumer()

        // when
        List stdoutArray = []
        List stderrArray = []
        task.go('vet ./...') {
            stdout { line ->
                stdoutArray << line
            }
            stderr { line ->
                stderrArray << line
            }
        }
        task.executeTask()

        // then
        assert task.exitValue == 0
        assert stdoutArray == stdouts
        assert stderrArray == stderrs
    }

    @Test
    void 'consuming stdout and stderr in run should succeed'() {
        // given
        letBuildManagerCallConsumer()

        // when
        List stdoutArray = []
        List stderrArray = []
        task.run('vet ./...') {
            stdout { line ->
                stdoutArray << line
            }
            stderr { line ->
                stderrArray << line
            }
        }
        task.executeTask()

        // then
        assert task.exitValue == 0
        assert stdoutArray == stdouts
        assert stderrArray == stderrs
    }

    @Test
    void 'consuming stdout with go should succeed'() {
        // given
        letBuildManagerCallConsumer()

        // when
        List stdoutArray = []
        task.go('vet ./...') {
            stdout { line ->
                stdoutArray << line
            }
        }
        task.executeTask()

        // then
        assert stdoutArray == stdouts
    }

    @Test
    void 'consuming stdout with run should succeed'() {
        // given
        letBuildManagerCallConsumer()

        // when
        List stdoutArray = []
        task.run('golint -v -a') {
            stdout { line ->
                stdoutArray << line
            }
        }
        task.executeTask()

        // then
        assert stdoutArray == stdouts
    }

    @Test
    @WithResource('')
    void 'appending and writing to file should succeed'() {
        // given
        letBuildManagerCallConsumer()
        IOUtils.write(resource, 'append.txt', 'append\n');
        IOUtils.write(resource, 'write.txt', 'write\n');
        // when
        task.go('vet ./...') {
            stdout appendTo('append.txt')
            stderr writeTo(new File(resource, 'write.txt').absolutePath)
        }
        task.executeTask()

        // then
        assert new File(resource, 'append.txt').text == 'append\nstdout1\nstdout2\n'
        assert new File(resource, 'write.txt').text == 'stderr1\nstderr2\n'
    }

    @Test
    @WithResource('')
    void 'appending and writing to file should succeed when file not exist'() {
        // given
        letBuildManagerCallConsumer()
        // when
        task.go('vet ./...') {
            stdout appendTo('append.txt')
            stderr writeTo(new File(resource, 'write.txt').absolutePath)
        }
        task.executeTask()

        // then
        assert new File(resource, 'append.txt').text == 'stdout1\nstdout2\n'
        assert new File(resource, 'write.txt').text == 'stderr1\nstderr2\n'
    }

    @Test
    void 'setting environment should succeed'() {
        task.environment(['a': '1'])
        assert ReflectionUtils.getField(task, 'environment') == [a: '1']
        task.environment('b', '2')
        assert ReflectionUtils.getField(task, 'environment') == [a: '1', b: '2']
    }

    @Test
    void 'env in action should overwrite env in task'() {
        // given
        task.environment('a', '1')
        task.go('whatever1') {
            environment('a', '2')
        }
        task.go('whatever2')

        // when
        task.executeTask()

        // then
        verify(buildManager, times(2)).go(anyList(), captor.capture(), any(Consumer), any(Consumer), eq(false))

        assert captor.allValues == [[a: '2'], [a: '1']]
    }

    @Test
    void 'continueOnFailure in action should overwrite that in task'() {
        // given
        task.continueOnFailure = true
        task.go('whatever1') {
            continueOnFailure = false
        }
        task.go('whatever2')

        // when
        task.executeTask()

        // then
        verify(buildManager).go(eq(['whatever1']), anyMap(), any(Consumer), any(Consumer), eq(false))
        verify(buildManager).go(eq(['whatever2']), anyMap(), any(Consumer), any(Consumer), eq(true))
    }

    @Test
    void 'consumer in action should overwrite that in task'() {
        // given
        def list1 = []
        def list2 = []
        task.stdout {
            list1.add(it)
        }
        task.go('whatever1') {
            stdout {
                list2.add(it)
            }
        }

        letBuildManagerCallConsumer()

        // when
        task.executeTask()
        // then
        assert list1 == []
        assert list2 == ['stdout1', 'stdout2']

        // when
        task.go('whatever2')
        task.executeTask()
        // then
        assert list1 == ['stdout1', 'stdout2']
        assert list2 == ['stdout1', 'stdout2', 'stdout1', 'stdout2']
    }
}
