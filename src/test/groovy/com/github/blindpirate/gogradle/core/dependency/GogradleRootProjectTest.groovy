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

import org.junit.Test

class GogradleRootProjectTest {
    GogradleRootProject rootProject = new GogradleRootProject()

    @Test(expected = IllegalStateException)
    void 'exception should be thrown in second initialization'() {
        rootProject.name = 'name'
        rootProject.initSingleton('name', null)
    }

    @Test
    void 'some methods should throw UnsupportOperationException'() {
        assertUnsupport { rootProject.setDir('') }
        assertUnsupport { rootProject.getUpdateTime() }
        assertUnsupport { rootProject.formatVersion() }
        assertUnsupport { rootProject.getVersion() }
        assertUnsupport { rootProject.clone() }
    }

    @Test
    void 'it should be resolved to itself'() {
        assert rootProject.resolve(null).is(rootProject)
    }

    @Test
    void 'toNotation should succeed'() {
        assert rootProject.toLockedNotation() == [name: 'GOGRADLE_ROOT']
    }

    @Test
    void 'equals and hashCode should succeed'() {
        assert rootProject != new GogradleRootProject()
        assert rootProject.hashCode() != new GogradleRootProject().hashCode()
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
