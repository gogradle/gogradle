package com.github.blindpirate.gogradle.core.pack

import org.junit.Test

class LocalFileSystemPackageModuleTest {
    @Test
    void 'create cascading vendor package should success'() {
        def uri = getClass().getClassLoader().getResource('vendor_test').toURI();
        def rootDir = new File(uri)

        LocalFileSystemPackageModule module = LocalFileSystemPackageModule.fromFileSystem('testpackage', rootDir);

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
