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

class GithubPackagePathResolverTest {

    GithubPackagePathResolver resolver = new GithubPackagePathResolver()

    @Test
    void 'resolving name should succeed'() {
        // when
        GolangPackage result = resolver.produce('github.com/a/b').get()

        // then
        assert result.pathString == 'github.com/a/b'
        assertVcsTypeUrlAndRootPath(result)
    }

    @Test
    void 'resolving an incomplete name should succeed'() {
        // when
        GolangPackage result = resolver.produce('github.com/a').get()
        // then
        assert result instanceof IncompleteGolangPackage
    }


    @Test
    void 'resolving a long name should succeed'() {
        // when
        GolangPackage info = resolver.produce('github.com/a/b/c').get()

        // then
        assert info.pathString == 'github.com/a/b/c'
        assertVcsTypeUrlAndRootPath(info)
    }

    @Test
    void 'resolving a long long name should succeed'() {
        // when
        String wtf = 'github.com/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z'
        GolangPackage info = resolver.produce(wtf).get()

        // then
        assert info.pathString == wtf
        assertVcsTypeUrlAndRootPath(info)
    }

    @Test
    void 'empty result should be returned if it does not host on github'() {
        assert !resolver.produce('golang.org/x/tool').isPresent()
    }

    void assertVcsTypeUrlAndRootPath(GolangPackage info) {
        assert info.vcsType == VcsType.GIT
        assert info.vcsType == VcsType.GIT
        assert info.urls == ['https://github.com/a/b.git', 'git@github.com:a/b.git']
        assert info.rootPathString == 'github.com/a/b'
    }
}
