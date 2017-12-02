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

package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

import static com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage.of

class LocalDirectoryGolangPackageTest {
    @Test
    void 'a local package should resolve its descendants and ancestors successfully'() {
        LocalDirectoryGolangPackage pkg = of('this/is/root', 'this/is/root', '')
        GolangPackage desc = pkg.resolve('this/is/root/sub').get()
        GolangPackage ance = pkg.resolve('this/is').get()
        assert desc instanceof LocalDirectoryGolangPackage
        assert desc.pathString == 'this/is/root/sub'
        assert desc.rootPathString == 'this/is/root'
        assert desc.dir == ''

        assert ance instanceof IncompleteGolangPackage
        assert ance.pathString == 'this/is'
    }

    @Test
    void 'a local sub package should resolve its descendants and ancestors successfully'() {
        LocalDirectoryGolangPackage pkg = of('this/is/root', 'this/is/root/sub', '')
        GolangPackage desc = pkg.resolve('this/is/root/sub/sub').get()
        GolangPackage ance = pkg.resolve('this/is').get()
        GolangPackage root = pkg.resolve('this/is/root').get()

        assert desc instanceof LocalDirectoryGolangPackage
        assert desc.pathString == 'this/is/root/sub/sub'
        assert desc.rootPathString == 'this/is/root'
        assert desc.dir == ''

        assert root instanceof LocalDirectoryGolangPackage
        assert root.pathString == 'this/is/root'
        assert root.rootPathString == 'this/is/root'
        assert root.dir == ''

        assert ance instanceof IncompleteGolangPackage
        assert ance.pathString == 'this/is'
    }

    @Test
    void 'equality check should succeed'() {
        LocalDirectoryGolangPackage pkg = of('this/is/root', 'this/is/root/sub', '')
        assert pkg != null
        assert pkg == pkg
        assert pkg != VcsGolangPackage.builder()
                .withRootPath('this/is/root')
                .withPath("this/is/root/sub")
                .withRepository(GolangRepository.newOriginalRepository(VcsType.GIT, ['url']))
                .build()
        assert pkg == of('this/is/root', 'this/is/root/sub', '')
        assert pkg != of('this/is/root', 'this/is/root', '')
        assert pkg != of('this/is', 'this/is/root/sub', '')
        assert pkg != of('this/is/root', 'this/is/root/sub', 'anotherDir')
    }
}
