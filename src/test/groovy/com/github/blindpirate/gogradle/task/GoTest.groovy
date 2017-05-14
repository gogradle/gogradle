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
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor

import java.util.function.Consumer

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class GoTest extends TaskTest {

    Go task

    @Before
    void setUp() {
        task = buildTask(Go)
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
        assert captor.getValue().is(ReflectionUtils.getStaticField(Go,'DO_NOTHING'))
    }

    @Test
    void 'consuming stdout and stderr should succeed'() {
        boolean stdoutIsVisited, stderrIsVisited
        task.go('test -v github.com/my/package', { stdout, stderr ->
            stdoutIsVisited = true
            stderrIsVisited = true
        })

        assert stderrIsVisited
        assert stdoutIsVisited
    }

    @Test
    void 'consuming stdout should succeed'() {
        boolean stdoutIsVisited, stderrIsVisited
        task.go('test -v github.com/my/package', { stdout ->
            stdoutIsVisited = true
        })

        assert !stderrIsVisited
        assert stdoutIsVisited
    }

    @Test
    void 'closure with more than 2 args should be ignored'() {
        boolean stdoutIsVisited, stderrIsVisited
        task.run('golint -v -a', { a, b, c ->
            stdoutIsVisited = true
            stderrIsVisited = true
        })

        assert !stderrIsVisited
        assert !stdoutIsVisited
    }
}
