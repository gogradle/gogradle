package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

import java.util.concurrent.ConcurrentHashMap

import static com.github.blindpirate.gogradle.core.cache.AbstractCacheTest.*

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
        cache = new PersistentCacheForTest()
        storageFile = new File(resource, ".gogradle/cache/${PersistentCacheForTest.simpleName}.bin")
        Mockito.when(project.getRootDir()).thenReturn(resource)
    }

    void prepareSerializationFile() {
        ConcurrentHashMap map = new ConcurrentHashMap()
        map[1] = new GolangCloneableForTest(value: 1)
        map[2] = new GolangCloneableForTest(value: 2)

        IOUtils.serialize(map, storageFile)
    }

    @Test
    void 'loading from serialization file should succeed'() {
        prepareSerializationFile()
        cache.load()
        assert cache.get(1, null).value == 1
        assert cache.get(2, null).value == 2
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

    class PersistentCacheForTest extends PersistentCache<Integer, GolangCloneableForTest> {
        PersistentCacheForTest() {
            super(PersistentCacheTest.this.project)
        }
    }
}
