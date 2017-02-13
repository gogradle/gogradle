package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.IncompleteGolangPackage
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

class IBMDevOpsPackagePathResolverTest {
    IBMDevOpsPackagePathResolver resolver = new IBMDevOpsPackagePathResolver()

    @Test
    void 'package not starting with hub.jazz.net should be rejected'() {
        assert !resolver.produce('github/a/b').isPresent()
        assert !resolver.produce('hub.jazz.net2/a/b').isPresent()
    }

    @Test
    void 'incomplete package should be produced correctly'() {
        assert resolver.produce('hub.jazz.net').get() instanceof IncompleteGolangPackage
        assert resolver.produce('hub.jazz.net/git').get() instanceof IncompleteGolangPackage
        assert resolver.produce('hub.jazz.net/git/user').get() instanceof IncompleteGolangPackage
    }

    @Test
    void 'package should be resolved correctly'() {
        GolangPackage pkg = resolver.produce('hub.jazz.net/git/user/package/submodule').get()
        assert pkg.path == 'hub.jazz.net/git/user/package/submodule'
        assert pkg.rootPath == 'hub.jazz.net/git/user/package'
        assert pkg.vcsType == VcsType.GIT
        assert pkg.urls == ['https://hub.jazz.net/git/user/package']
    }
}
