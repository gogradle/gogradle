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

package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangCloneable
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.apache.commons.collections4.map.LRUMap
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.util.function.Function

import static com.github.blindpirate.gogradle.core.cache.AbstractCacheTest.GolangCloneableForTest
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class PersistentCacheTest {
    File resource

    @Mock
    Project project

    PersistenceCache cache

    File storageFile

    @Before
    void setUp() {
        when(project.getProjectDir()).thenReturn(resource)
        storageFile = new File(resource, '.gogradle/cache/cache.bin')
        cache = new PersistenceCache(project, 'cache.bin')
    }

    Map prepareCacheMap() {
        LRUMap map = new LRUMap()
        map[buildCloneable(1)] = buildCloneable(1)
        map[buildCloneable(2)] = buildCloneable(2)
        return map
    }

    @Test
    void 'loading from serialization file should succeed'() {
        IOUtils.serialize(prepareCacheMap(), storageFile)
        cache.load()
        assert cache.get(buildCloneable(1), null) == buildCloneable(1)
        assert cache.get(buildCloneable(2), null) == buildCloneable(2)
    }

    @Test
    void 'nothing should happen if serialization does not exist'() {
        cache.load()
        assert ReflectionUtils.getField(cache, 'container').isEmpty()
    }

    @Test
    void 'nothing should happen if exception occurs in deserialization'() {
        IOUtils.write(storageFile, '')
        cache.load()
        assert ReflectionUtils.getField(cache, 'container').isEmpty()
    }

    @Test
    void 'cache should be persisted'() {
        // when
        cache.get(buildCloneable(1), new Function() {
            @Override
            Object apply(Object o) {
                buildCloneable(1)
            }
        })
        cache.save()
        cache.load()
        // then
        assert ReflectionUtils.getField(cache, 'container').size() == 1
        assert cache.get(buildCloneable(1), null) == buildCloneable(1)
    }

    @Test
    void 'exception should be recorded if IOException occurs'() {
        // given
        Logger logger = mock(Logger)
        ReflectionUtils.setStaticFinalField(PersistenceCacheHelper, 'LOGGER', logger)
        cache = new PersistenceCache(project, '.')
        // when
        cache.save()
        // then
        assert ReflectionUtils.getField(cache, 'container').isEmpty()
        verify(logger).warn('Exception in serializing dependency cache, skip.')
    }

    GolangCloneable buildCloneable(int value) {
        return new GolangCloneableForTest(value: value)
    }

}
