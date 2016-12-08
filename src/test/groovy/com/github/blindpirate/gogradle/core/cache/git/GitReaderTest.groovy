package com.github.blindpirate.gogradle.core.cache.git

import com.github.blindpirate.gogradle.AccessWeb
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.cache.CacheManager
import com.github.blindpirate.gogradle.core.dependency.GitDependency
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

import java.nio.file.Paths

import static com.github.blindpirate.gogradle.util.FileUtils.forceDelete
import static org.mockito.Matchers.any
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GitReaderTest {
    @InjectMocks
    GitReader gitReader
    @Mock
    GolangPluginSetting setting
    @Mock
    CacheManager cacheManager
    @Mock
    GitDependency dependency

    @Test
    @AccessWeb
    public void 'nonexistent repo should be cloned'() {
        File tmpDir = new File("build/tmp/nonexistent-${UUID.randomUUID()}")
        when(dependency.getName()).thenReturn('github.com/blindpirate/test-for-gogradle')
        when(dependency.getUrl()).thenReturn('https://github.com/blindpirate/test-for-gogradle.git')
        when(cacheManager.getGlobalCachePath(any())).thenReturn(Paths.get(tmpDir.absolutePath))
        gitReader.resolve(dependency)

        assert tmpDir.toPath().resolve('.git').toFile().exists()
        forceDelete(tmpDir)
    }

}
