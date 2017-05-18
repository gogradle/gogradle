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

package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleRunner
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GogradleRootProjectTest {
    @Mock
    Project project
    @Mock
    File rootDir

    GogradleRootProject rootProject

    @Before
    void setUp() {
        when(project.getRootDir()).thenReturn(rootDir)
        when(rootDir.exists()).thenReturn(true)
        when(rootDir.isDirectory()).thenReturn(true)
        rootProject = new GogradleRootProject(project)
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown in second initialization'() {
        rootProject.name = 'name'

        assert rootProject.name == 'name'
        rootProject.name = 'name'
    }

    @Test
    void 'some methods should throw UnsupportOperationException'() {
        assertUnsupport { rootProject.setDir('') }
        assertUnsupport { rootProject.getUpdateTime() }
        assertUnsupport { rootProject.formatVersion() }
        assertUnsupport { rootProject.getVersion() }
        assertUnsupport { rootProject.clone() }
        assertUnsupport { rootProject.toLockedNotation() }
    }

    @Test
    void 'it should be resolved to itself'() {
        assert rootProject.resolve(null).is(rootProject)
    }

    @Test
    void 'equals and hashCode should succeed'() {
        assert rootProject != new GogradleRootProject(project)
        assert rootProject == rootProject
        assert rootProject.hashCode() != new GogradleRootProject(project).hashCode()
    }

    void assertUnsupport(Closure c) {
        try {
            c.call()
        }
        catch (Exception e) {
            return
        }
        assert false
    }
}
