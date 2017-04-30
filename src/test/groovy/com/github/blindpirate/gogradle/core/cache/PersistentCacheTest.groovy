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
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito

import java.util.function.Function

import static com.github.blindpirate.gogradle.core.cache.AbstractCacheTest.GolangCloneableForTest
import static org.mockito.Mockito.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class PersistentCacheTest {
    File resource

    @Mock
    Project project

    PersistentCache cache

    File storageFile

    @Before
    void setUp() {
        storageFile = new File(resource, "cache.bin")
        cache = new PersistentCacheForTest(storageFile)
        when(project.getRootDir()).thenReturn(resource)
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
        ReflectionUtils.setStaticFinalField(PersistentCache, 'LOGGER', logger)
        cache = new PersistentCacheForTest(resource)
        ArgumentCaptor captor = ArgumentCaptor.forClass(Throwable)
        // when
        cache.save()
        // then
        assert ReflectionUtils.getField(cache, 'container').isEmpty()
        verify(logger).warn('Exception in serializing dependency cache, skip.')
    }

    GolangCloneable buildCloneable(int value) {
        return new GolangCloneableForTest(value: value);
    }

    class PersistentCacheForTest extends PersistentCache<GolangCloneableForTest, GolangCloneableForTest> {
        PersistentCacheForTest(File file) {
            super(file)
        }
    }
}
