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
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.util.function.Consumer

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GoTest extends TaskTest {

    Go task

    List stdouts = ['stdout1', 'stdout2']

    List stderrs = ['stderr1', 'stderr2']

    File resource

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
        when(buildManager.run(anyList(), anyMap(), any(Consumer), any(Consumer), isNull())).thenAnswer(answer)
        when(buildManager.go(anyList(), anyMap(), any(Consumer), any(Consumer), isNull())).thenAnswer(answer)
    }

    @Test
    void 'do-nothing-consumer should succeed'() {
        Consumer c = ReflectionUtils.getStaticField(Go, 'DO_NOTHING')
        c.accept(1)
    }

    @Test
    void 'go command should succeed'() {
        // when
        task.go('build -o "output name"')
        // then
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List)
        verify(buildManager).go(captor.capture(), anyMap(), any(Consumer), any(Consumer), isNull())
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
        // then
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List)
        verify(buildManager).run(captor.capture(), anyMap(), any(Consumer), any(Consumer), isNull())
        assert captor.getValue() == ['golint', '-v', '-a']
    }

    @Test(expected = MissingMethodException)
    void 'exception should be thrown if args number of run is more than 1'() {
        task.run('a', 'b')
    }

    @Test
    void 'invoking doAddDefaultAction should succeed'() {
        task.doAddDefaultAction()
    }

    @Test
    void 'setting continueWhenFail should succeed'() {
        // given
        task.continueWhenFail = true
        // when
        task.go('test -v github.com/my/package')
        // then
        ArgumentCaptor<Consumer> captor = ArgumentCaptor.forClass(Consumer)
        verify(buildManager).go(anyList(), anyMap(), any(Consumer), any(Consumer), captor.capture())
        assert captor.getValue().is(ReflectionUtils.getStaticField(Go, 'DO_NOTHING'))
    }

    @Test
    void 'consuming stdout and stderr in go should succeed'() {
        // given
        letBuildManagerCallConsumer()

        // when
        List stdout = []
        List stderr = []
        def retcode = task.go('vet ./...', { line ->
            stdout << line
        }, { line ->
            stderr << line
        })

        // then
        assert retcode == 0
        assert stdout == stdouts
        assert stderr == stderrs
    }

    @Test
    void 'consuming stdout and stderr in run should succeed'() {
        // given
        letBuildManagerCallConsumer()

        // when
        List stdout = []
        List stderr = []
        def retcode = task.run('vet ./...', { line ->
            stdout << line
        }, { line ->
            stderr << line
        })

        // then
        assert retcode == 0
        assert stdout == stdouts
        assert stderr == stderrs
    }

    @Test
    void 'consuming stdout with go should succeed'() {
        // given
        letBuildManagerCallConsumer()

        // when
        List stdout = []
        task.go('vet ./...', { line ->
            stdout << line
        })

        // then
        assert stdout == ['stdout1', 'stdout2']
    }

    @Test
    void 'consuming stdout with run should succeed'() {
        // given
        letBuildManagerCallConsumer()

        // when
        List stdout = []
        task.run('golint -v -a', { line ->
            stdout << line
        })

        // then
        assert stdout == ['stdout1', 'stdout2']
    }

    @Test
    @WithResource('')
    void 'appending and writing to file should succeed'() {
        // given
        letBuildManagerCallConsumer()
        IOUtils.write(resource, 'append.txt', 'append\n');
        IOUtils.write(resource, 'write.txt', 'write\n');
        // when
        task.go('vet ./...', task.appendTo('append.txt'), task.writeTo(new File(resource, 'write.txt').absolutePath))

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
        task.go('vet ./...', task.appendTo('append.txt'), task.writeTo(new File(resource, 'write.txt').absolutePath))

        // then
        assert new File(resource, 'append.txt').text == 'stdout1\nstdout2\n'
        assert new File(resource, 'write.txt').text == 'stderr1\nstderr2\n'
    }

    @Test
    void 'setting environment should succeed'() {
        task.environment(['a': '1'])
        assert ReflectionUtils.getField(task, 'overallEnvironment') == [a: '1']
        task.environment('b', '2')
        assert ReflectionUtils.getField(task, 'overallEnvironment') == [a: '1', b: '2']
    }

}
