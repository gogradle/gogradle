package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static com.github.blindpirate.gogradle.util.ProcessUtils.runProcessWithCurrentClasspath
import static com.github.blindpirate.gogradle.util.ReflectionUtils.*
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class DefaultGlobalCacheManagerTest {

    File resource

    @InjectMocks
    DefaultGlobalCacheManager cacheManager
    @Mock
    NotationDependency notationDependency
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    Project project

    ExecutorService threadPool = Executors.newFixedThreadPool(10)

    @Before
    void setUp() {
        when(notationDependency.getName()).thenReturn("concurrenttest")
        setField(cacheManager, "gradleHome", resource.getAbsoluteFile().toPath())
    }

    @Test
    void "validation of global cache directory should success"() {
        // when
        cacheManager.ensureGlobalCacheExistAndWritable()
        // then
        assert resource.toPath().resolve(DefaultGlobalCacheManager.GO_BINARAY_CACHE_PATH).toFile().exists()
        assert resource.toPath().resolve(DefaultGlobalCacheManager.GO_BINARAY_CACHE_PATH).toFile().exists()
        assert resource.toPath().resolve(DefaultGlobalCacheManager.GO_LOCKFILES_PATH).toFile().exists()
    }

    @Test
    void 'multi-thread access should be safe'() {
        // when
        int i = 0
        Callable thread = {
            cacheManager.runWithGlobalCacheLock(notationDependency, { 1000.times { i++ } })
        }
        List futures = (1..10).collect { threadPool.submit(thread as Callable) }

        // then
        futures.each { it.get() }
        assert i == 10 * 1000
    }

    @Test
    void 'multi-process access should be serialized'() {
        // when
        String filePath = resource.toPath().resolve('shared').toAbsolutePath().toString()

        Callable runOneProcess = {
            cacheManager.runWithGlobalCacheLock(notationDependency, {
                runProcessWithCurrentClasspath(CounterProcess, [filePath], [:])
            })
        }

        List futures = (1..10).collect {
            threadPool.submit(runOneProcess as Callable)
        }

        // then
        assert futures.any { it.get().code == CounterProcess.SUCCESS }
    }

    class CounterProcess {
        public static final int ERROR = 1
        public static final int SUCCESS = 0

        public static void main(String[] args) {
            // every process should not see other one's file
            File file = new File(args[0])
            if (file.exists()) {
                fail()
            } else {
                FileUtils.touch(file)
                Thread.sleep(5)
                FileUtils.forceDelete(file)
                success()
            }
        }

        static void success() {
            System.exit(SUCCESS)
        }

        static void fail() {
            System.exit(ERROR)
        }
    }


}