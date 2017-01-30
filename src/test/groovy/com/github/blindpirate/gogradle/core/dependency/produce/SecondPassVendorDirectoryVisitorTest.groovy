package com.github.blindpirate.gogradle.core.dependency.produce

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.core.MockInjectorSupport
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.VendorOnlyProduceStrategy
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.core.pack.UnrecognizedPackagePathResolver
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class SecondPassVendorDirectoryVisitorTest extends MockInjectorSupport {
    PackagePathResolver packagePathResolver = new UnrecognizedPackagePathResolver()
    @Mock
    VendorOnlyProduceStrategy vendorOnlyProduceStrategy
    @Mock
    ResolvedDependency hostDependency

    File resource

    @Before
    void setUp() {
        when(injector.getInstance(VendorOnlyProduceStrategy)).thenReturn(vendorOnlyProduceStrategy)
        IOUtils.write(resource, 'vendor/a/main.go', '')
        IOUtils.write(resource, 'vendor/b/vendor/c/main.go', '')
    }

    @Test
    void 'unrecognized package should be produced correctly'() {
        def visitor = new SecondPassVendorDirectoryVisitor(hostDependency, resource.toPath().resolve('vendor'), packagePathResolver)
        IOUtils.walkFileTreeSafely(resource.toPath().resolve('vendor'), visitor)
        assert visitor.dependencies.size() == 2
        VendorResolvedDependency a = visitor.dependencies.find { it.name == 'a' }
        VendorResolvedDependency b = visitor.dependencies.find { it.name == 'b' }
        assert [a, b].every {
            it instanceof VendorResolvedDependency
            it.hostDependency == hostDependency
        }
        assert a.relativePathToHost.toString() == 'vendor/a'
        assert b.relativePathToHost.toString() == 'vendor/b'
    }

}


