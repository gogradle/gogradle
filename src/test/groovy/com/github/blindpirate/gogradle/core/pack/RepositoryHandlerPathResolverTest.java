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

package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.GolangRepositoryHandler;
import com.github.blindpirate.gogradle.core.GolangRepositoryPattern;
import com.github.blindpirate.gogradle.core.IncompleteGolangPackage;
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryHandlerPathResolverTest {
    @Mock
    GolangRepositoryHandler repositoryHandler;

    @Mock
    GolangRepositoryPattern repository;

    @Mock
    GolangRepositoryPattern incompleteRepository;

    RepositoryHandlerPathResolver resolver;

    @Before
    public void setUp() {
        resolver = new RepositoryHandlerPathResolver(repositoryHandler);
    }

    @Test
    public void only_root_package_should_be_produced() {
        when(repositoryHandler.findMatchedRepository("this/is/root/sub")).thenReturn(Optional.empty());
        when(repositoryHandler.findMatchedRepository("this/is/root")).thenReturn(Optional.of(repository));
        when(repositoryHandler.findMatchedRepository("this/is")).thenReturn(Optional.empty());
        when(repositoryHandler.findMatchedRepository("this")).thenReturn(Optional.empty());
        assert !resolver.produce("this/is").isPresent();
        assert !resolver.produce("this").isPresent();

        assertProducePath("this/is/root/sub");
        assertProducePath("this/is/root");
    }

    @Test
    public void incomplete_package_should_be_produced() {
        when(repositoryHandler.findMatchedRepository("this/is/root/sub")).thenReturn(Optional.empty());
        when(repositoryHandler.findMatchedRepository("this/is/root")).thenReturn(Optional.of(repository));
        when(repositoryHandler.findMatchedRepository("this/is")).thenReturn(Optional.of(incompleteRepository));
        when(repositoryHandler.findMatchedRepository("this")).thenReturn(Optional.of(incompleteRepository));
        when(incompleteRepository.isIncomplete()).thenReturn(true);

        assertProducePath("this/is/root/sub");
        assertProducePath("this/is/root");

        assertIncompletePath("this/is");
        assertIncompletePath("this");
    }

    @Test
    public void incomplete_package_should_not_be_produced_by_sub_path() {
        when(repositoryHandler.findMatchedRepository("this/is/root")).thenReturn(Optional.empty());
        when(repositoryHandler.findMatchedRepository("this/is")).thenReturn(Optional.of(incompleteRepository));
        when(repositoryHandler.findMatchedRepository("this")).thenReturn(Optional.of(incompleteRepository));
        when(incompleteRepository.isIncomplete()).thenReturn(true);


        assertIncompletePath("this/is");
        assertIncompletePath("this");
        assert !resolver.produce("this/is/root").isPresent();
    }

    void assertIncompletePath(String path) {
        IncompleteGolangPackage pkg = (IncompleteGolangPackage) resolver.produce(path).get();
        assert pkg.getPathString().equals(path);
    }

    void assertProducePath(String path) {
        Mockito.reset(repository);
        when(repository.getVcsType()).thenReturn(VcsType.GIT);
        when(repository.getUrl("this/is/root")).thenReturn("url");
        VcsGolangPackage pkg = (VcsGolangPackage) resolver.produce(path).get();
        assert pkg.getRootPathString().equals( "this/is/root");
        assert pkg.getPathString().equals( path);
        assert pkg.getVcsType() == VcsType.GIT;
        assert pkg.getUrls() .equals(Lists.newArrayList("url"));

        Mockito.reset(repository);
        when(repository.getDir("this/is/root")).thenReturn("dir");
        LocalDirectoryGolangPackage localPkg = (LocalDirectoryGolangPackage) resolver.produce(path).get();
        assert localPkg.getRootPathString().equals( "this/is/root");
        assert localPkg.getPathString().equals(path);
        assert localPkg.getDir().equals("dir");
    }
}
