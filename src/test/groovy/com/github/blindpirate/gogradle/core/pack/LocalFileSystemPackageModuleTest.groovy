package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithResource
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('vendor_test.zip')
class LocalFileSystemPackageModuleTest {
    @Test
    void 'create cascading vendor package should success'() {
        def rootDir = new File("build/tmp/resource/vendor_test")

        LocalFileSystemModule module = LocalFileSystemModule.fromFileSystem('testpackage', rootDir);

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
