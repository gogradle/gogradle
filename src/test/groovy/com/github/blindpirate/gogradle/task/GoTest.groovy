package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.Go
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor

import java.util.function.Consumer

import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.isNull
import static org.mockito.Mockito.verify

@RunWith(GogradleRunner)
class GoTest extends TaskTest {

    Go task

    @Before
    void setUp() {
        task = buildTask(Go)
    }

    @Test
    void 'go command on ToolTask should succeed'() {
        // when
        task.go('build -o "output name"')
        // then
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List)
        verify(buildManager).go(captor.capture(), isNull(), any(Consumer), any(Consumer), isNull())
        assert captor.getValue() == ['build', '-o', 'output name']
    }

    @Test(expected = MissingMethodException)
    void 'exception should be thrown if args number of go is more than 1'() {
        task.go('a', 'b')
    }

    @Test
    void 'run command on ToolTask should succeed'() {
        // when
        task.run('golint -v -a')
        // then
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List)
        verify(buildManager).run(captor.capture(), isNull(), any(Consumer), any(Consumer), isNull())
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
        task.continueWhenFail = true
        assert ReflectionUtils.getField(task, 'continueWhenFail')
    }

    @Test
    void 'consuming stdout and stderr should succeed'() {
        boolean stdoutIsVisited, stderrIsVisited
        task.run('golint -v -a', { stdout, stderr ->
            stdoutIsVisited = true
            stderrIsVisited = true
        })

        assert stderrIsVisited
        assert stdoutIsVisited
    }

    @Test
    void 'consuming stdout should succeed'() {
        boolean stdoutIsVisited, stderrIsVisited
        task.run('golint -v -a', { stdout ->
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
