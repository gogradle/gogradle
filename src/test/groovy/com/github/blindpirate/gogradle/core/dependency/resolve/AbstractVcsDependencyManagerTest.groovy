package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Paths
import java.util.concurrent.Callable

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class AbstractVcsDependencyManagerTest {

    public static Answer callCallableAnswer = new Answer() {
        @Override
        Object answer(InvocationOnMock invocation) throws Throwable {
            return invocation.getArgument(1).call()
        }
    }

    @Mock
    AbstractVcsDependencyManager manager
    @Mock
    GlobalCacheManager cacheManager
    @Mock
    ResolvedDependency hostDependency
    @Mock
    VendorResolvedDependency vendorResolvedDependency

    File resource
    File src
    File dest


    @Before
    void setUp() {
        src = IOUtils.mkdir(resource, 'src')
        dest = IOUtils.mkdir(resource, 'dest')

        IOUtils.write(src, 'vendor/root/package/main.go', 'This is main.go')

        when(cacheManager.runWithGlobalCacheLock(any(GolangDependency), any(Callable))).thenAnswer(callCallableAnswer)
        ReflectionUtils.setField(manager, 'globalCacheManager', cacheManager)
        when(manager.install(any(ResolvedDependency), any(File))).thenCallRealMethod()
    }

    @Test
    void 'installing a vendor dependency hosting in vcs dependency should succeed'() {
        // given
        when(hostDependency.getName()).thenReturn('host')
        when(cacheManager.getGlobalCachePath('host')).thenReturn(src.toPath())
        when(vendorResolvedDependency.getHostDependency()).thenReturn(hostDependency)
        when(vendorResolvedDependency.getRelativePathToHost()).thenReturn(Paths.get('vendor/root/package'))
        // when
        manager.install(vendorResolvedDependency, dest)
        // then
        assert dest.toPath().resolve('main.go').toFile().getText() == 'This is main.go'
    }
}
