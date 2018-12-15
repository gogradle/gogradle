/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.IncompleteGolangPackage
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

class IBMDevOpsPackagePathResolverTest {
    IBMDevOpsPackagePathResolver resolver = new IBMDevOpsPackagePathResolver()

    @Test
    void 'package not starting with hub_jazz_net should be rejected'() {
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
        assert pkg.pathString == 'hub.jazz.net/git/user/package/submodule'
        assert pkg.rootPathString == 'hub.jazz.net/git/user/package'
        assert pkg.vcsType == VcsType.GIT
        assert pkg.urls == ['https://hub.jazz.net/git/user/package']
    }
}
