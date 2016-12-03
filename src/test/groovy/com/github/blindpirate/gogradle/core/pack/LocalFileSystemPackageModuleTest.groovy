package com.github.blindpirate.gogradle.core.pack

import org.junit.Test

class LocalFileSystemPackageModuleTest {
    @Test
    void 'create cascading vendor package should success'() {
        def uri = getClass().getClassLoader().getResource('testpackage').toURI();
        def rootDir = new File(uri)

        LocalFileSystemPackageModule module = LocalFileSystemPackageModule.fromFileSystem('testpackage', rootDir);

        assert module.dependencies

    }
}
