package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
        task.go('build -o "output name"')
        verify(buildManager).go(['build', '-o', 'output name'], null)
    }

    @Test(expected = MissingMethodException)
    void 'exception should be thrown if args number of go is more than 1'() {
        task.go('a', 'b')
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if args number of go is zero'() {
        task.go()
    }

    @Test
    void 'run command on ToolTask should succeed'() {
        task.run('golint -v -a')
        verify(buildManager).run(['golint', '-v', '-a'], null)
    }

    @Test(expected = MissingMethodException)
    void 'exception should be thrown if args number of run is more than 1'() {
        task.run('a', 'b')
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if args number of run is zero'() {
        task.run()
    }

    @Test
    void 'invoking doAddDefaultAction should succeed'() {
        task.doAddDefaultAction()
    }
}
