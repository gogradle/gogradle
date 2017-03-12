package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import org.apache.commons.lang3.tuple.Pair
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GoBuildTaskTest extends TaskTest {
    GoBuildTask task

    @Before
    void setUp() {
        task = buildTask(GoBuildTask)
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
    void 'custom action should be expanded when added in doLast'() {
        // when
        task.doLast {}
        // then
        assert task.actions.size() == 3
        assert task.actions.any { it instanceof GoExecutionAction }
    }

    @Test
    void 'custom action should be expanded when added in doFirst'() {
        // when
        task.doFirst {}
        // then
        assert task.actions.size() == 3
        assert task.actions.any { it instanceof GoExecutionAction }
    }

    @Test(expected = UnsupportedOperationException)
    void 'left shift should be unsupported'() {
        task << {}
    }

    @Test
    void 'execution of custom action should succeed'() {
        // when
        task.doLast {
            go 'build -o output'
        }
        task.actions.each { it.execute(task) }
        // then
        assert task.actions.size() == 3
        assert task.actions.any { it instanceof GoExecutionAction }
        assert task.currentEnv == null
        verify(buildManager).go(['build', '-o', 'output'], [GOOS: 'darwin', GOARCH: 'amd64', GOEXE: '', GOPATH: 'build_gopath'])
        verify(buildManager).go(['build', '-o', 'output'], [GOOS: 'linux', GOARCH: '386', GOEXE: '', GOPATH: 'build_gopath'])
        verify(buildManager).go(['build', '-o', 'output'], [GOOS: 'windows', GOARCH: 'amd64', GOEXE: '.exe', GOPATH: 'build_gopath'])
    }

    @Test
    void 'other methods in GoExecutionAction should succeed'() {
        // when
        Closure c = {
            go 'build -o output'
        }

        task.doLast(c)
        // then
        task.actions.each { it.contextualise(null) }
        task.actions.each { assert it.classLoader == c.class.classLoader }
    }

    @Test(expected = NullPointerException)
    void 'exception should be thrown if closure throws an exception'() {
        task.doLast {
            throw new NullPointerException()
        }
        task.actions.each { it.execute(task) }
    }
}
