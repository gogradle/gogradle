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

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.GolangRepository
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.*
import com.github.blindpirate.gogradle.vcs.VcsAccessor
import com.github.blindpirate.gogradle.vcs.VcsNotationDependency
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Key
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.Paths
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static com.github.blindpirate.gogradle.core.cache.DefaultGlobalCacheManager.GO_BINARAY_CACHE_PATH
import static com.github.blindpirate.gogradle.core.cache.DefaultGlobalCacheManager.REPO_CACHE_PATH
import static com.github.blindpirate.gogradle.util.IOUtils.mkdir
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
    VcsNotationDependency notationDependency
    @Mock
    VcsResolvedDependency resolvedDependency
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
        when(accessor.getRemoteUrl(new File(resource, 'go/gopath/github.com/user/package'))).thenReturn('url')
        ReflectionUtils.setField(cacheManager, "gradleHome", resource.toPath())

        cacheManager.ensureGlobalCacheExistAndWritable()
    }

    @Test
    void 'getting files in global binary cache directory should succeed'() {
        assert cacheManager.getGlobalGoBinCacheDir('1.7.1') == new File(resource, 'go/binary/1.7.1')
    }

    @Test
    void "validation of global cache directory should succeed"() {
        // when
        cacheManager.ensureGlobalCacheExistAndWritable()
        // then
        assert new File(resource, GO_BINARAY_CACHE_PATH).exists()
        assert new File(resource, REPO_CACHE_PATH).exists()
    }

    @Test
    void 'lock file should be initialized'() {
        // when
        cacheManager.startSession(pkg)
        cacheManager.endSession()

        // then
        GlobalCacheMetadata metadata = DataExchange.parseYaml(new File(resource, 'go/repo/github.com/user/package/gogradle-metadata'), GlobalCacheMetadata)
        assert metadata.getPackage() == 'github.com/user/package'
        assert metadata.repositories.size() == 1
        assert metadata.repositories[0].lastUpdatedTime == 0
        assert metadata.repositories[0].vcs == 'git'
        assert metadata.repositories[0].original
        assert metadata.repositories[0].urls == ['git@github.com:user/package.git', 'https://github.com/user/package.git']
        assert metadata.repositories[0].dir ==~ /[0-9a-z]{32}/
    }

    @Test
    void 'lock file should be updated'() {
        // when
        cacheManager.startSession(pkg)
        cacheManager.repoUpdated()
        cacheManager.endSession()
        // then
        GlobalCacheMetadata metadata = DataExchange.parseYaml(new File(resource, 'go/repo/github.com/user/package/gogradle-metadata'), GlobalCacheMetadata)
        assert metadata.getPackage() == 'github.com/user/package'
        assert metadata.repositories.size() == 1
        assert System.currentTimeMillis() - metadata.repositories[0].lastUpdatedTime < 5 * 1000
        assert metadata.repositories[0].vcs == 'git'
        assert metadata.repositories[0].original
        assert metadata.repositories[0].urls == ['git@github.com:user/package.git', 'https://github.com/user/package.git']
        assert metadata.repositories[0].dir ==~ /[0-9a-z]{32}/
    }

    @Test
    void 'repositories should be added to lock file successfully'() {
        // given
        cacheManager.startSession(pkg)
        cacheManager.endSession()

        // when
        GolangRepository anotherRepo = GolangRepository.newSubstitutedRepository(VcsType.MERCURIAL, ['anotherUrl'])
        pkg = VcsGolangPackage.builder().withPath('github.com/user/package').withRootPath('github.com/user/package').withRepository(anotherRepo).build()
        cacheManager.startSession(pkg)
        cacheManager.endSession()

        // then
        GlobalCacheMetadata metadata = DataExchange.parseYaml(new File(resource, 'go/repo/github.com/user/package/gogradle-metadata'), GlobalCacheMetadata)
        assert metadata.getPackage() == 'github.com/user/package'
        assert metadata.repositories.size() == 2
        assert metadata.repositories[1].lastUpdatedTime == 0
        assert !metadata.repositories[1].original
        assert metadata.repositories[1].vcs == 'hg'
        assert metadata.repositories[1].urls == ['anotherUrl']
        assert metadata.repositories[1].dir ==~ /[0-9a-z]{32}/
        assert metadata.repositories[0].dir != metadata.repositories[1].dir
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown if IOException occurs in updating lock file'() {
        IOUtils.forceMkdir(new File(resource, 'go/repo/github.com/user/package/gogradle-metadata'))
        cacheManager.startSession(pkg)
        cacheManager.endSession()
    }

    String getMetadataYaml(long updateTime) {
        return """---
apiVersion: "0.8.0"
package: "github.com/user/package"
repositories:
- vcs: "git"
  urls:
  - "git@github.com:user/package.git"
  - "https://github.com/user/package.git"
  lastUpdatedTime: ${updateTime}
  dir: 1a2b3c4d
  original: true 
"""
    }

    @Test
    void 'repo should be considered as out-of-date'() {
        // given
        when(setting.getGlobalCacheSecond()).thenReturn(1L)
        write(resource, 'go/repo/github.com/user/package/gogradle-metadata', getMetadataYaml(System.currentTimeMillis() - 200 * 1000L))

        // then
        cacheManager.startSession(pkg)
        assert !cacheManager.currentRepositoryIsUpToDate()
        cacheManager.endSession()
    }

    @Test
    void 'repo should be considered as out-of-date if url not match current url in repo'() {
        // given
        when(setting.getGlobalCacheSecond()).thenReturn(1L)
        write(resource, 'go/repo/github.com/user/package/gogradle-metadata', getMetadataYaml(System.currentTimeMillis()))

        // when
        GolangRepository anotherRepo = GolangRepository.newSubstitutedRepository(VcsType.MERCURIAL, ['anotherUrl'])
        pkg = VcsGolangPackage.builder().withPath('github.com/user/package').withRootPath('github.com/user/package').withRepository(anotherRepo).build()
        cacheManager.startSession(pkg)

        // then
        assert !cacheManager.currentRepositoryIsUpToDate()
        cacheManager.endSession()
    }

    @Test
    void 'repo should be considered as up-to-date'() {
        // given
        when(setting.getGlobalCacheSecond()).thenReturn(1L)
        write(resource, 'go/repo/github.com/user/package/gogradle-metadata', getMetadataYaml(System.currentTimeMillis()))
        // then
        cacheManager.startSession(pkg)
        assert cacheManager.currentRepositoryIsUpToDate()
        cacheManager.endSession()
    }

    @Test
    void 'getting repo dir of a package should succeed'() {
        // given
        write(resource, 'go/repo/github.com/user/package/gogradle-metadata', getMetadataYaml(0L))
        // then
        cacheManager.startSession(pkg)
        assert cacheManager.getGlobalCacheRepoDir() == new File(resource, 'go/repo/github.com/user/package/1a2b3c4d')
        cacheManager.endSession()
        assert ReflectionUtils.getField(cacheManager, 'sessions').get() == null
    }

    @Test
    void 'empty result should be returned if metadata not exist'() {
        assert !cacheManager.getMetadata(Paths.get('inexistent')).isPresent()
    }

    @Test
    void 'empty result should be returned if it is locked by another process'() {
        // given
        write(resource, 'go/repo/github.com/user/package/gogradle-metadata', '----\n')
        Thread.start {
            new ProcessUtils().runProcessWithCurrentClasspath(LockProcess,
                    [new File(resource, 'go/repo/github.com/user/package/gogradle-metadata').getAbsolutePath()],
                    [:])
        }

        // wait for another process to start and lock
        Thread.sleep(5000)

        assert !cacheManager.getMetadata(Paths.get('github.com/user/package')).isPresent()
    }

    @Test
    void 'empty result should be returned if IOException occurs in metadata reading'() {
        mkdir(resource, 'go/repo/github.com/user/package/gogradle-metadata')
        assert !cacheManager.getMetadata(Paths.get('github.com/user/package')).isPresent()
    }

    class LockProcess {
        static void main(String[] args) {
            FileChannel channel = null
            FileLock lock = null
            File lockFile = new File(args[0])
            try {
                channel = new RandomAccessFile(lockFile, "rw").getChannel()
                lock = channel.lock()
                Thread.sleep(10000)
            } finally {
                if (lock != null) {
                    println("${new Date()}: release ${lockFile.absolutePath}")
                    lock.release()
                }
                if (channel != null) {
                    channel.close()
                }
            }
        }
    }

    @Test
    void 'multi-thread access should be safe'() {
        // when
        int i = 0
        Callable thread = {
            cacheManager.startSession(pkg)
            1000.times { i++ }
            cacheManager.endSession()
        }
        List futures = (1..10).collect { threadPool.submit(thread as Callable) }

        // then
        futures.each { it.get() }
        assert i == 10 * 1000
    }

    @Test
    void 'multi-process access should be serialized'() {
        // when
        ProcessUtils processUtils = new ProcessUtils()

        Callable runOneProcess = {
            cacheManager.startSession(pkg)
            def result = processUtils.runProcessWithCurrentClasspath(CounterProcess,
                    [new File(resource, 'go/repo/github.com/user/package/testfile').absolutePath], [:])
            cacheManager.endSession()

            return result
        }

        List futures = (1..10).collect {
            threadPool.submit(runOneProcess as Callable)
        }

        // then
        assert futures.every { it.get().code == CounterProcess.SUCCESS }
    }

    class CounterProcess {
        public static final int ERROR = 2
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