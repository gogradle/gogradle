package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.task.go.GoBuildTask
import com.github.blindpirate.gogradle.task.go.GoExecutionAction
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
        task.setTargetPlatform('darwin-amd64,linux-386,windows-amd64')
        when(buildManager.getBuildGopath()).thenReturn('build_gopath')
    }

    @Test
    void 'adding default action should succeed'() {
        // when
        task.addDefaultActionIfNoCustomActions()
        task.actions.each { it.execute(task) }
        // then
        assert task.actions.size() == 1
        verify(buildManager).go(['build', '-o', './.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}'], [GOOS: 'darwin', GOARCH: 'amd64', GOEXE: '', GOPATH: 'build_gopath'], null, null, null)
        verify(buildManager).go(['build', '-o', './.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}'], [GOOS: 'linux', GOARCH: '386', GOEXE: '', GOPATH: 'build_gopath'], null, null, null)
        verify(buildManager).go(['build', '-o', './.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}'], [GOOS: 'windows', GOARCH: 'amd64', GOEXE: '.exe', GOPATH: 'build_gopath'], null, null, null)
    }

    @Test
    void 'default action should not be added if there is customized action'() {
        // when
        task.setTargetPlatform("${Os.hostOs}-${Arch.hostArch}")
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
        assert task.env == null
        verify(buildManager).go(['build', '-o', 'output'], [GOOS: 'darwin', GOARCH: 'amd64', GOEXE: '', GOPATH: 'build_gopath'], null, null, null)
        verify(buildManager).go(['build', '-o', 'output'], [GOOS: 'linux', GOARCH: '386', GOEXE: '', GOPATH: 'build_gopath'], null, null, null)
        verify(buildManager).go(['build', '-o', 'output'], [GOOS: 'windows', GOARCH: 'amd64', GOEXE: '.exe', GOPATH: 'build_gopath'], null, null, null)
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

    @Test(expected = IllegalStateException)
    void 'setting illegal target platform should result in an exception'() {
        task.targetPlatform = 'a-b,'
    }

    @Test(expected = IllegalArgumentException)
    void 'setting illegal os or arch should result in an exception'() {
        task.targetPlatform = 'a-b'
    }

    @Test(expected = IllegalArgumentException)
    void 'os must located at left'() {
        task.targetPlatform = 'amd64-linux'
    }

    @Test
    void 'setting target platform should succeed'() {
        task.targetPlatform = 'windows-amd64, linux-amd64, linux-386'
        assert task.targetPlatforms[0].left == Os.WINDOWS
        assert task.targetPlatforms[0].right == Arch.AMD64
        assert task.targetPlatforms[1].left == Os.LINUX
        assert task.targetPlatforms[1].right == Arch.AMD64
        assert task.targetPlatforms[2].left == Os.LINUX
        assert task.targetPlatforms[2].right == Arch.I386
    }

    @Test
    void 'pattern matching targetPlatform should be correct'() {
        assert isValidTargetPlatform('a-b')
        assert isValidTargetPlatform('\t a-b \n')
        assert !isValidTargetPlatform(' a -b ')
        assert !isValidTargetPlatform('\ta-b,\n')
        assert !isValidTargetPlatform('a-b,')
        assert !isValidTargetPlatform(' a-b, ')
        assert !isValidTargetPlatform(',a-b,')
        assert isValidTargetPlatform('a-b,1-a,c-d')
        assert isValidTargetPlatform('\t\t\na-b\n ,\n 1-a\t\n , c-2d  ')
    }

    boolean isValidTargetPlatform(String value) {
        return GoBuildTask.TARGET_PLATFORM_PATTERN.matcher(value).matches()
    }
}
