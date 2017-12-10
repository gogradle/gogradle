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

import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Test

import static com.github.blindpirate.gogradle.core.GolangRepository.newOriginalRepository
import static com.github.blindpirate.gogradle.core.GolangRepository.newSubstitutedRepository

class VcsGolangPackageTest {
    VcsGolangPackage vcsGolangPackage = MockUtils.mockVcsPackage()

    @Test
    void 'toString should print properties successfully'() {
        String s = vcsGolangPackage.toString()
        assert s.contains("path='github.com/user/package/a'")
        assert s.contains('vcsType=GIT')
        assert s.contains('https://github.com/user/package.git')
        assert s.contains("rootPath='github.com/user/package'")
    }

    @Test
    void 'path shorter than root should be incomplete'() {
        assert vcsGolangPackage.resolve('github.com').get() instanceof IncompleteGolangPackage
        assert vcsGolangPackage.resolve('github.com/user').get() instanceof IncompleteGolangPackage
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if resolving an package without common prefix'() {
        assert vcsGolangPackage.resolve('github.com/anotheruser')
    }

    @Test
    void 'path longer than root should have have the same root'() {
        // when
        GolangPackage packageWithLongerPath = vcsGolangPackage.resolve('github.com/user/package/a/b').get()
        // then
        assert packageWithLongerPath.pathString == 'github.com/user/package/a/b'
        assertRootPathAndSoOn(packageWithLongerPath)
    }

    @Test
    void 'path equal to root should have have the same root'() {
        // when
        GolangPackage rootPackage = vcsGolangPackage.resolve('github.com/user/package').get()
        // then
        assert rootPackage.pathString == 'github.com/user/package'
        assertRootPathAndSoOn(rootPackage)
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if both originalInfo and substitutedInfo are null'() {
        VcsGolangPackage.builder().withPath('root')
                .withRootPath('root')
                .build()
    }

    @Test
    void 'equality check should succeed'() {
        assert !vcsGolangPackage.equals(null)
        assert vcsGolangPackage.equals(vcsGolangPackage)
        assert vcsGolangPackage != LocalDirectoryGolangPackage.of('github.com/user/package', 'github.com/user/pavkage/a', '')
        assert vcsGolangPackage == MockUtils.mockVcsPackage()
        assert vcsGolangPackage != VcsGolangPackage.builder()
                .withPath('github.com/user/package/a')
                .withRootPath('github.com/user/package')
                .withRepository(newOriginalRepository(VcsType.MERCURIAL, ['git@github.com:user/package.git', 'https://github.com/user/package.git']))
                .build()
        assert vcsGolangPackage != VcsGolangPackage.builder()
                .withPath('github.com/user/package/a')
                .withRootPath('github.com/user/package')
                .withRepository(newOriginalRepository(VcsType.GIT, []))
                .build()
        assert vcsGolangPackage != VcsGolangPackage.builder()
                .withPath('github.com/user/package/b')
                .withRootPath('github.com/user/package')
                .withRepository(newOriginalRepository(VcsType.GIT, ['git@github.com:user/package.git', 'https://github.com/user/package.git']))
                .build()
        assert vcsGolangPackage != VcsGolangPackage.builder()
                .withPath('github.com/user/package/a')
                .withRootPath('github.com/user')
                .withRepository(newOriginalRepository(VcsType.GIT, ['git@github.com:user/package.git', 'https://github.com/user/package.git']))
                .build()
        assert vcsGolangPackage == VcsGolangPackage.builder()
                .withPath('github.com/user/package/a')
                .withRootPath('github.com/user/package')
                .withRepository(newSubstitutedRepository(VcsType.GIT, ['git@github.com:user/package.git', 'https://github.com/user/package.git']))
                .build()
    }

    @Test
    void 'hashCode should succeed'() {
        assert vcsGolangPackage.hashCode() != LocalDirectoryGolangPackage.of('github.com/user/package', 'github.com/user/pavkage/a', '').hashCode()
        assert vcsGolangPackage.hashCode() == MockUtils.mockVcsPackage().hashCode()
    }

    void assertRootPathAndSoOn(GolangPackage golangPackage) {
        assert golangPackage instanceof VcsGolangPackage
        assert golangPackage.rootPathString == 'github.com/user/package'
        assert golangPackage.vcsType == VcsType.GIT
        assert golangPackage.urls == ['git@github.com:user/package.git', 'https://github.com/user/package.git']
    }
}
