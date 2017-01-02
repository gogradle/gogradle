package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

class GolangPackageTest {

    @Test
    void 'toString should print properties successfully'() {
        GolangPackage golangPackage = GolangPackage.builder()
                .withPath('root/package')
                .withVcsType(VcsType.Git)
                .withUrl('repoUrl')
                .withRootPath('root')
                .withStandard(true)
                .build()
        String s = golangPackage.toString()
        assert s.contains("path='root/package'")
        assert s.contains('vcsType=Git')
        assert s.contains('repoUrl')
        assert s.contains("rootPath='root'")
        assert s.contains('standard=true')
    }

}
