package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import org.apache.commons.lang3.tuple.Pair
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class BuildTaskTest extends TaskTest {
    BuildTask task

    @Before
    void setUp() {
        task = buildTask(BuildTask)
        when(setting.getTargetPlatforms()).thenReturn(
                [Pair.of(Os.DARWIN, Arch.AMD64),
                 Pair.of(Os.LINUX, Arch.I386),
                 Pair.of(Os.WINDOWS, Arch.AMD64)])
        when(buildManager.getBuildGopath()).thenReturn('build_gopath')
    }

    @Test
    void 'adding default action should succeed'() {
        // when
        task.addDefaultActionIfNoCustomActions()
        task.actions.each { it.execute(task) }
        // then
        assert task.actions.size() == 3
        verify(buildManager).go(['build', '-o', './gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}'], [GOOS: 'darwin', GOARCH: 'amd64', GOEXE: '', GOPATH: 'build_gopath'])
        verify(buildManager).go(['build', '-o', './gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}'], [GOOS: 'linux', GOARCH: '386', GOEXE: '', GOPATH: 'build_gopath'])
        verify(buildManager).go(['build', '-o', './gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}'], [GOOS: 'windows', GOARCH: 'amd64', GOEXE: '.exe', GOPATH: 'build_gopath'])
    }

    @Test
    void 'default action should not be added if there is customized action'() {
        // when
        when(setting.getTargetPlatforms()).thenReturn([Pair.of(Os.hostOs, Arch.hostArch)])
        task.doLast {}
        task.addDefaultActionIfNoCustomActions()
        // then
        assert task.actions.size() == 1
    }

    @Test
    void 'custom action should be expanded when added'() {
        // when
        task.doLast {}
        // then
        assert task.actions.size() == 3
        assert task.actions.any { it instanceof GoExecutionAction }
    }
}
