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

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangRepositoryHandler
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.vcs.VcsType
import com.github.blindpirate.gogradle.vcs.git.GolangRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class RepositoryHandlerPathResolverTest {
    @Mock
    GolangRepositoryHandler repositoryHandler

    @Mock
    GolangRepository repository

    RepositoryHandlerPathResolver resolver

    @Before
    void setUp() {
        resolver = new RepositoryHandlerPathResolver(repositoryHandler)

        when(repositoryHandler.findMatchedRepository('this/is/root/sub')).thenReturn(GolangRepository.EMPTY_INSTANCE)
        when(repositoryHandler.findMatchedRepository('this/is/root')).thenReturn(repository)
        when(repositoryHandler.findMatchedRepository('this/is')).thenReturn(GolangRepository.EMPTY_INSTANCE)
        when(repositoryHandler.findMatchedRepository('this')).thenReturn(GolangRepository.EMPTY_INSTANCE)
    }

    @Test
    void 'only root package should be produced'() {
        assert !resolver.produce('this/is').isPresent()
        assert !resolver.produce('this').isPresent()

        assertProducePath('this/is/root/sub')
        assertProducePath('this/is/root')
    }

    void assertProducePath(String path) {
        Mockito.reset(repository)
        when(repository.getVcsType()).thenReturn(VcsType.GIT)
        when(repository.getUrl('this/is/root')).thenReturn('url')
        VcsGolangPackage pkg = resolver.produce(path).get()
        assert pkg.rootPathString == 'this/is/root'
        assert pkg.pathString == path
        assert pkg.vcsType == VcsType.GIT
        assert pkg.urls == ['url']


        Mockito.reset(repository)
        when(repository.getDir('this/is/root')).thenReturn('dir')
        LocalDirectoryGolangPackage localPkg = resolver.produce(path).get()
        assert localPkg.rootPathString == 'this/is/root'
        assert localPkg.pathString == path
        assert localPkg.getDir() == 'dir'
    }
}
