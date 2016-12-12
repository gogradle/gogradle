package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito

import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

@RunWith(GogradleRunner)
public class DefaultCacheManagerTest {

    File resource

    @InjectMocks
    DefaultCacheManager cacheManager
    @Mock
    GolangDependency dependency

    @Before
    void replaceGradleHome() {
    }

    @Test
    void "validation of global cache directory should success"() {
        cacheManager.ensureGlobalCacheExistAndWritable();
    }

    @Test
    @WithResource('')
    void 'multithread access should be safe'() {
        // given:
        Mockito.when(dependency.getName()).thenReturn("multithreadtest")
        ReflectionUtils.setField(cacheManager, "gradleHome", resource.getAbsoluteFile().toPath())

        // when:
        int i = 0
        CountDownLatch latch = new CountDownLatch(10)
        10.times {
            Thread.start {
                cacheManager.runWithGlobalCacheLock(dependency, { 1000.times { i++ } } as Callable)
                latch.countDown()
            }
        }

        // then:
        latch.await()
        assert i == 10 * 1000
    }

    @Test
    void 'multi process access should be safe'() {
        // TODO how to run a multi process test?
    }


}