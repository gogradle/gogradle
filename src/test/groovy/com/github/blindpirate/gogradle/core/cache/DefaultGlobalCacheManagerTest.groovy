package com.github.blindpirate.gogradle.core.cache

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.DataExchange
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency
import com.github.blindpirate.gogradle.vcs.GitMercurialResolvedDependency
import com.github.blindpirate.gogradle.vcs.VcsAccessor
import com.google.inject.Key
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static com.github.blindpirate.gogradle.core.cache.DefaultGlobalCacheManager.GO_BINARAY_CACHE_PATH
import static com.github.blindpirate.gogradle.core.cache.DefaultGlobalCacheManager.GO_METADATA_PATH
import static com.github.blindpirate.gogradle.util.IOUtils.write
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class DefaultGlobalCacheManagerTest {

    File resource

    DefaultGlobalCacheManager cacheManager
    @Mock
    GolangPluginSetting setting
    @Mock
    GitMercurialNotationDependency notationDependency
    @Mock
    GitMercurialResolvedDependency resolvedDependency
    @Mock
    Project project
    @Mock
    VcsAccessor accessor

    VcsGolangPackage pkg = MockUtils.mockRootVcsPackage()

    ExecutorService threadPool = Executors.newFixedThreadPool(10)

    @Before
    void setUp() {
        cacheManager = new DefaultGlobalCacheManager(setting)
        when(notationDependency.getName()).thenReturn(pkg.getPathString())
        when(notationDependency.getUrls()).thenReturn(pkg.getUrls())
        when(notationDependency.getPackage()).thenReturn(pkg)
        when(GogradleGlobal.getInstance((Key) any(Key))).thenReturn(accessor)
        ReflectionUtils.setField(cacheManager, "gradleHome", resource.toPath())
    }

    @Test
    void 'getting files in global binary cache directory should succeed'() {
        assert cacheManager.getGlobalGoBinCache('1.7.1') == new File(resource, 'go/binary/1.7.1').toPath()
    }

    @Test
    void "validation of global cache directory should succeed"() {
        // when
        cacheManager.ensureGlobalCacheExistAndWritable()
        // then
        assert new File(resource, GO_BINARAY_CACHE_PATH).exists()
        assert new File(resource, GO_BINARAY_CACHE_PATH).exists()
        assert new File(resource, GO_METADATA_PATH).exists()
    }

    @Test
    void 'lock file should be initialized'() {
        // when
        cacheManager.runWithGlobalCacheLock(notationDependency, {} as Callable)
        // then
        GlobalCacheMetadata metadata = DataExchange.parseYaml(new File(resource, 'go/metadata/github.com%2Fuser%2Fpackage'), GlobalCacheMetadata)
        assert metadata.lastUpdateTime == 0
        assert metadata.lastUpdateUrl == null
        assert metadata.vcs == 'git'
        assert metadata.originalUrls == ['git@github.com:user/package.git', 'https://github.com/user/package.git']
    }

    @Test
    void 'lock file should be updated successfully'() {
        // when
        cacheManager.runWithGlobalCacheLock(notationDependency, {
            cacheManager.updateCurrentDependencyLock(notationDependency)
            cacheManager.updateCurrentDependencyLock(notationDependency)
        } as Callable)
        // then
        GlobalCacheMetadata metadata = DataExchange.parseYaml(new File(resource, 'go/metadata/github.com%2Fuser%2Fpackage'), GlobalCacheMetadata)
        assert (-1000L..1000L).contains(metadata.getLastUpdateTime() - System.currentTimeMillis())
    }

    @Test
    void 'dependency should be considered as out-of-date'() {
        // given
        when(setting.getGlobalCacheSecond()).thenReturn(1L)
        write(resource, 'go/metadata/github.com%2Fuser%2Fpackage', """
originalUrls:
  - git@github.com:user/package.git
  - https://github.com/user/package.git
vcs:
  git
lastUpdated:
  time: ${System.currentTimeMillis() - 2000}
  url: https://github.com/user/package.git
""")
        // then
        cacheManager.runWithGlobalCacheLock(notationDependency, {
            assert cacheManager.currentDependencyIsOutOfDate(notationDependency)
        } as Callable)
    }

    @Test
    void 'dependency should be considered as up-to-date'() {
        // given
        when(setting.getGlobalCacheSecond()).thenReturn(1L)
        write(resource, 'go/metadata/github.com%2Fuser%2Fpackage', """
originalUrls:
  - git@github.com:user/package.git
  - https://github.com/user/package.git
vcs:
  git
lastUpdated:
  time: ${System.currentTimeMillis()}
  url: https://github.com/user/package.git
""")
        // then
        cacheManager.runWithGlobalCacheLock(notationDependency, {
            assert !cacheManager.currentDependencyIsOutOfDate(notationDependency)
        } as Callable)
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
        String filePath = new File(resource, 'shared').getAbsolutePath()

        ProcessUtils processUtils = new ProcessUtils()

        Callable runOneProcess = {
            cacheManager.runWithGlobalCacheLock(notationDependency, {
                processUtils.runProcessWithCurrentClasspath(CounterProcess, [filePath], [:])
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

        static void main(String[] args) {
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