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

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.lock.DefaultLockedDependencyManager
import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.mockDependency
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class GoInitTest extends TaskTest {

    File resource

    GoInit task

    @Mock
    ExternalDependencyFactory externalDependencyFactory

    @Mock
    DefaultLockedDependencyManager defaultLockedDependencyManager

    @Mock
    GolangConfiguration build

    @Mock
    GolangConfiguration test

    @Before
    void setUp() {
        task = buildTask(GoInit)
        ReflectionUtils.setField(task, 'externalDependencyFactories', [defaultLockedDependencyManager, externalDependencyFactory])
        when(configurationManager.getByName('build')).thenReturn(build)
        when(configurationManager.getByName('test')).thenReturn(test)
        when(project.getProjectDir()).thenReturn(resource)
        [build, test].each { when(it.dependencies).thenReturn(GolangDependencySet.empty()) }
    }

    @Test
    void 'initialization should be skipped if gogradle.lock exists'() {
        // given
        IOUtils.write(resource, 'gogradle.lock', '')
        // when
        task.init()
        // then
        verify(externalDependencyFactory, times(0)).canRecognize(any(File))
    }

    @Test
    void 'initialization should be skipped if dependencies in build.gradle exist'() {
        // given
        when(build.hasFirstLevelDependencies()).thenReturn(true)
        // when
        task.init()
        // then
        verify(externalDependencyFactory, times(0)).canRecognize(any(File))
    }

    @Test
    void 'init by external model should succeed'() {
        // given
        when(externalDependencyFactory.canRecognize(any(File))).thenReturn(true)
        when(externalDependencyFactory.extractNotations(resource, 'build')).thenReturn(
                [[name: 'a', transitive: false], [name: 'b', url: 'url']]
        )
        when(externalDependencyFactory.extractNotations(resource, 'test')).thenReturn([[name: 'c']])
        // when
        task.init()
        // then
        assert new File(resource, 'build.gradle').text.contains('''\
dependencies {
    golang {
        build name:'a', transitive:false
        build name:'b', url:'url'
        test name:'c'
    }
}''')
    }

    @Test
    void 'init by source code should succeed'() {
        // given
        GolangDependencySet ab = asGolangDependencySet(mockDependency('a'), mockDependency('b'))
        GolangDependencySet bc = asGolangDependencySet(mockDependency('b'), mockDependency('c'))
        when(visitor.visitSourceCodeDependencies(gogradleRootProject, resource, 'build')).thenReturn(ab)
        when(visitor.visitSourceCodeDependencies(gogradleRootProject, resource, 'test')).thenReturn(bc)
        // when
        task.init()
        // then
        assert new File(resource, 'build.gradle').text.contains('''\
dependencies {
    golang {
        build name:'a'
        build name:'b'
        test name:'c'
    }
}''')
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if unrecognized notation type exists'() {
        // given
        when(externalDependencyFactory.canRecognize(any(File))).thenReturn(true)
        when(externalDependencyFactory.extractNotations(resource, 'build')).thenReturn([[name: 'a', unknownType: 1]])
        when(externalDependencyFactory.extractNotations(resource, 'test')).thenReturn([])
        // when
        task.init()
    }

    @Test
    void 'it should depend on prepare task'() {
        assertTaskDependsOn(task, GolangTaskContainer.PREPARE_TASK_NAME)
    }
}
