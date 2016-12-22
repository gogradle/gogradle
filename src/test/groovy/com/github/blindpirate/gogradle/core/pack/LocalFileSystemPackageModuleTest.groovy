package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.DependencyHelper
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyFactory
import com.github.blindpirate.gogradle.core.dependency.resolve.VendorDependencyFactory
import com.google.common.base.Optional
import com.google.inject.Injector
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Paths

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('vendor_test.zip')
class LocalFileSystemPackageModuleTest {

    File resource

    @Mock
    Injector injector

    PackageNameResolver resolver = new GithubPackageNameResolver() {
        @Override
        public Optional<PackageInfo> produce(String name) {
            if (Paths.get(name).getNameCount() < 3) {
                return Optional.absent()
            } else {
                return super.produce(name);
            }
        }
    }

    @Before
    void setUp() {
        DependencyHelper.INJECTOR_INSTANCE = injector
        when(injector.getInstance(DependencyFactory.class))
                .thenReturn(new VendorDependencyFactory(resolver))
    }

    @Test
    void 'create cascading vendor package should success'() {
        LocalFileSystemModule module = LocalFileSystemModule.fromFileSystem('testpackage', resource);

        assert module.dependencies.any {
            it.package.name == 'github.com/e/f'
        }
        assert module.dependencies.any {
            it.package.name == 'github.com/e/g'
        }

        def ef = module.dependencies.find { it.package.name == 'github.com/e/f' }
        assert ef.package.dependencies.any { it.package.name == 'github.com/j/k' }

    }
}
