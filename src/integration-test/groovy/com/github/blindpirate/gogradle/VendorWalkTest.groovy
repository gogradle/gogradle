package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.pack.LocalFileSystemDependency
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('vendor_test.zip')
class VendorWalkTest extends GogradleModuleSupport {

    File resource

    @Test
    void 'create cascading vendor package should success'() {
        LocalFileSystemDependency module = LocalFileSystemDependency.fromLocal('testpackage', resource);

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