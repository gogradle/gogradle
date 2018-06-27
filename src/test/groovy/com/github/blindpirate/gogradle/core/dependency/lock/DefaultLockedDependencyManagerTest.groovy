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

package com.github.blindpirate.gogradle.core.dependency.lock

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.external.AbstractExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.support.WithProject
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks

import static com.github.blindpirate.gogradle.util.MockUtils.mockMultipleInterfaces
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithProject
class DefaultLockedDependencyManagerTest extends AbstractExternalDependencyFactoryTest {
    Project project

    @InjectMocks
    DefaultLockedDependencyManager manager
    GolangDependency dependency1 = mockMultipleInterfaces(NotationDependency, ResolvedDependency)
    GolangDependency dependency2 = mockMultipleInterfaces(NotationDependency, ResolvedDependency)
    GolangDependency dependency3 = mockMultipleInterfaces(NotationDependency, ResolvedDependency)
    GolangDependency dependency4 = mockMultipleInterfaces(NotationDependency, ResolvedDependency)

    String LOCK_FILE_NAME = 'gogradle.lock'
    String warning = ReflectionUtils.getStaticField(DefaultLockedDependencyManager, "WARNING")
    String gogradleDotLock =
            """${warning}---
apiVersion: "${GogradleGlobal.GOGRADLE_VERSION}"
dependencies:
  build:
  - name: "a"
    version: "v1"
    transitive: false
  - name: "b"
    version: "v2"
    transitive: false
  test:
  - name: "a"
    version: "v2"
    transitive: false
  - name: "c"
    version: "v3"
    transitive: false
"""

    @Before
    void setUp() {
        ReflectionUtils.setField(manager, 'project', project)
        when(dependency1.getName()).thenReturn('b')
        when(dependency2.getName()).thenReturn('a')
        when(dependency3.getName()).thenReturn('c')
        when(dependency4.getName()).thenReturn('a')
    }

    void prepareGogradleDotLock() {
        when(mapNotationParser.parse([name: 'b', version: 'v2', transitive: false])).thenReturn(dependency1)
        when(mapNotationParser.parse([name: 'a', version: 'v1', transitive: false])).thenReturn(dependency2)
        when(mapNotationParser.parse([name: 'c', version: 'v3', transitive: false])).thenReturn(dependency3)
        when(mapNotationParser.parse([name: 'a', version: 'v2', transitive: false])).thenReturn(dependency4)
        IOUtils.write(project.getProjectDir(), LOCK_FILE_NAME, gogradleDotLock)
    }


    @Test
    @WithResource('')
    void 'reading other gogradle project\'s dependencies should succeed'() {
        // given
        prepareGogradleDotLock()
        // when
        GolangDependencySet buildResult = manager.produce(parentDependency, project.rootDir, 'build')
        GolangDependencySet testResult = manager.produce(parentDependency, project.rootDir, 'test')
        // then
        assert buildResult.any { it.is(dependency1) }
        assert buildResult.any { it.is(dependency2) }
        assert testResult.any { it.is(dependency3) }
        assert testResult.any { it.is(dependency4) }
    }

    @Test
    void 'writing to gogradle.lock should succeed'() {
        // given
        when(dependency1.toLockedNotation()).thenReturn([name: 'b', version: 'v2'])
        when(dependency2.toLockedNotation()).thenReturn([name: 'a', version: 'v1'])
        when(dependency3.toLockedNotation()).thenReturn([name: 'c', version: 'v3'])
        when(dependency4.toLockedNotation()).thenReturn([name: 'a', version: 'v2'])

        // when
        manager.lock([dependency1, dependency2], [dependency3, dependency4])
        // then
        assert new File(project.getProjectDir(), LOCK_FILE_NAME).getText() == gogradleDotLock
    }

    @Test
    void 'existent gogradle.lock should be overwritten'() {
        IOUtils.write(project.getProjectDir(), LOCK_FILE_NAME, 'old file content')
        'writing to gogradle.lock should succeed'()
    }


}

