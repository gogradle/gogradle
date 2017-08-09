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
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.task.go.GoBuild
import org.gradle.api.internal.tasks.TaskContainerInternal
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static com.github.blindpirate.gogradle.util.StringUtils.capitalizeFirstLetter
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GoBuildTest extends TaskTest {
    GoBuild task

    @Mock
    TaskContainerInternal taskContainer

    Map tasks = [:]
    String taskName

    @Before
    void setUp() {
        task = buildTask(GoBuild)
        taskName = 'build' + capitalizeFirstLetter(Os.getHostOs().toString()) + capitalizeFirstLetter(Arch.getHostArch().toString())
        when(buildManager.getGopath()).thenReturn('project_gopath')
        when(setting.getPackagePath()).thenReturn('my/package')
        when(project.getTasks()).thenReturn(taskContainer)
        when(taskContainer.create(anyString(), (Class) any(Class))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                Go task = buildTask(Go)
                tasks.put(invocation.getArgument(0), task)
                return task
            }
        })
    }

    @Test
    void 'creating default task should succeed'() {
        // when
        task.afterEvaluate()
        // then


        assert tasks[taskName].commandLineArgs == ['GO', 'build', '-o', './.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}', 'my/package']
        assert tasks[taskName].environment == [GOOS: Os.getHostOs().toString(), GOARCH: Arch.getHostArch().toString(), GOEXE: Os.getHostOs().exeExtension()]
    }

    @Test
    void 'creating expanded tasks should succeed'() {
        // when
        task.setTargetPlatform('darwin-amd64,linux-386,windows-amd64')
        task.afterEvaluate()
        // then
        ['buildDarwinAmd64', 'buildLinux386', 'buildWindowsAmd64'].each {
            assert tasks[it].commandLineArgs == ['GO', 'build', '-o', './.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}', 'my/package']
        }

        assert tasks['buildDarwinAmd64'].environment == [GOOS: 'darwin', GOARCH: 'amd64', GOEXE: '']
        assert tasks['buildLinux386'].environment == [GOOS: 'linux', GOARCH: '386', GOEXE: '']
        assert tasks['buildWindowsAmd64'].environment == [GOOS: 'windows', GOARCH: 'amd64', GOEXE: '.exe']
    }

    @Test
    void 'setting continueWhenFail should succeed'() {
        // when
        task.setTargetPlatform(['darwin-amd64', 'linux-386'])
        task.setContinueWhenFail(true)
        task.afterEvaluate()
        // then
        ['buildDarwinAmd64', 'buildLinux386'].each {
            assert tasks[it].continueWhenFail
        }
    }

    @Test
    void 'custom action should succeed'() {
        // when
        task.go 'build -o output'
        task.afterEvaluate()
        // then
        assert tasks[taskName].commandLineArgs == ['GO', 'build', '-o', 'output']
    }

    @Test
    void 'setting output location should succeed'() {
        // when
        task.outputLocation = 'outputlocation'
        task.afterEvaluate()
        // then
        assert tasks[taskName].commandLineArgs == ['GO', 'build', '-o', 'outputlocation', 'my/package']
    }

    @Test
    void 'redirection should be inherited'() {
        // when
        task.run('cmd', task.devNull(), task.devNull())
        task.afterEvaluate()
        // then
        assert tasks[taskName].stdoutLineConsumer.is(task.stdoutLineConsumer)
        assert tasks[taskName].stderrLineConsumer.is(task.stderrLineConsumer)
    }

    @Test
    void 'custom environment should have higher priority'() {
        // when
        task.environment('GOOS', 'myOs')
        task.environment('GOARCH', 'myArch')
        task.environment('GOEXE', 'myExe')
        task.afterEvaluate()
        // then
        assert tasks[taskName].environment == [GOOS: 'myOs', GOARCH: 'myArch', GOEXE: 'myExe']
    }

    @Test(expected = IllegalStateException)
    void 'empty target platform should result in an exception'() {
        task.targetPlatform = []
    }

    @Test(expected = IllegalStateException)
    void 'setting illegal target platform list should result in an exception'() {
        task.targetPlatform = ['a-b,']
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
        assertTargetPlatforms()
    }

    @Test
    void 'duplicates should be removed'() {
        task.targetPlatform = 'windows-amd64, linux-amd64, linux-386,windows-amd64'
        assertTargetPlatforms()

        task.targetPlatform = ['windows-amd64', 'linux-amd64', 'linux-386', 'windows-amd64']
    }

    private void assertTargetPlatforms() {
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
        return GoBuild.TARGET_PLATFORMS_PATTERN.matcher(value).matches()
    }
}
