package com.github.blindpirate.gogradle.core.dependency.produce

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.MockInjectorSupport
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorDependency
import com.github.blindpirate.gogradle.core.exceptions.DependencyProductionException
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

import static java.util.Optional.of
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.calls
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class VendorDependencyFactoryTest extends MockInjectorSupport {
    File resource

    @InjectMocks
    VendorDependencyFactory factory

    @Mock
    PackagePathResolver resolver

    @Mock
    DependencyVisitor visitor

    @Mock
    ResolvedDependency resolvedDependency

    GolangPackage golangPackage = MockUtils.mockPackage()

    @Test
    void 'directory without vendor should produce an empty dependency set'() {
        assert factory.produce(resolvedDependency, resource).isEmpty()
    }

    @Test
    void 'producing a vendor dependency should succeed'() {
        // given
        when(resolver.produce('root')).thenReturn(of(GolangPackage.INCOMPLETE))
        when(resolver.produce('root/package')).thenReturn(of(golangPackage))
        when(injector.getInstance(DependencyVisitor)).thenReturn(visitor)
        when(visitor.visitVendorDependencies(any(ResolvedDependency), any(File)))
                .thenReturn(GolangDependencySet.empty())
        IOUtils.write(resource, 'vendor/root/package/main.go', '')

        // when
        GolangDependencySet set = factory.produce(resolvedDependency, resource)
        // then
        GolangDependency dependency = set.first()
        assert dependency instanceof VendorDependency
        assert ReflectionUtils.getField(dependency, 'hostDependency').is(resolvedDependency)
        assert ReflectionUtils.getField(dependency, 'relativePathToHost').toString() == 'vendor/root/package'
    }

    @Test
//(expected = DependencyProductionException)
    void 'IOException should be reported'() {
        factory.produce(resolvedDependency, new File('inexistence'))
    }
}
