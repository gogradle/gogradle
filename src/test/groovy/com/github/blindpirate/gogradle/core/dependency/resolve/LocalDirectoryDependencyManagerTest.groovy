package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyManager
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.util.function.Function

import static com.github.blindpirate.gogradle.core.dependency.resolve.AbstractVcsDependencyManagerTest.APPLY_FUNCTION_ANSWER
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class LocalDirectoryDependencyManagerTest {

    File resource

    LocalDirectoryDependencyManager localDirectoryDependencyManager

    @Mock
    LocalDirectoryDependency dependency
    @Mock
    ProjectCacheManager projectCacheManager
    @Mock
    ResolveContext context
    @Mock
    GolangDependencySet golangDependencySet

    File src
    File dest

    @Before
    void setUp() {
        src = IOUtils.mkdir(resource, 'src')
        dest = IOUtils.mkdir(resource, 'dest')
        when(dependency.getRootDir()).thenReturn(src)
        when(projectCacheManager.resolve(any(NotationDependency), any(Function))).thenAnswer(APPLY_FUNCTION_ANSWER)
        localDirectoryDependencyManager = new LocalDirectoryDependencyManager(projectCacheManager)
    }

    @Test
    void 'installing a local dependency should succeed'() {
        // given
        IOUtils.write(src, 'main.go', 'This is main.go')
        // when
        localDirectoryDependencyManager.install(dependency, dest)
        // then
        assert new File(dest, 'main.go').getText() == 'This is main.go'
    }

    @Test
    void 'resolving a local dependency should succeed'() {
        // given
        when(context.produceTransitiveDependencies(dependency, src)).thenReturn(golangDependencySet)
        // when
        LocalDirectoryDependency result = localDirectoryDependencyManager.resolve(context, dependency)
        // then
        assert result.is(dependency)
        verify(result).setDependencies(golangDependencySet)
    }
}
