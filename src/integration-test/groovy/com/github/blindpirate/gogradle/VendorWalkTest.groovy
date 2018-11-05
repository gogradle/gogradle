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

package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory
import com.github.blindpirate.gogradle.support.GogradleModuleSupport
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Test
import org.junit.runner.RunWith

import javax.inject.Inject

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('vendor_test.zip')
class VendorWalkTest extends GogradleModuleSupport {

    File resource

    @Inject
    VendorDependencyFactory factory

    @Test
    void 'create cascading vendor package should succeed'() {
        // when
        when(project.getProjectDir()).thenReturn(resource)
        LocalDirectoryDependency localPackage = LocalDirectoryDependency.fromLocal('testpackage', resource);
        GolangDependencySet dependencies = factory.produce(localPackage, resource)
        // then
        assert dependencies.size() == 3
        dependencies.each { assert it instanceof VendorResolvedDependency }
        dependencies.each { assert it.hostDependency == localPackage }

        assert dependencies.any {
            it.name == 'github.com/e/f' && it.relativePathToHost == 'vendor/github.com/e/f'
        }
        assert dependencies.any {
            it.name == 'github.com/e/g' && it.relativePathToHost == 'vendor/github.com/e/g'
        }
        assert dependencies.any {
            it.name == 'unrecognized/a' && it.relativePathToHost == 'vendor/unrecognized/a'
        }

        VendorResolvedDependency githubEF = dependencies.find { it.name == 'github.com/e/f' }
        assert githubEF.dependencies.size() == 1

        VendorResolvedDependency githubJK = githubEF.dependencies.first()
        assert githubJK.name == 'github.com/j/k'
        assert githubJK.relativePathToHost == 'vendor/github.com/e/f/vendor/github.com/j/k'
        assert githubJK.hostDependency == localPackage

        VendorResolvedDependency unrecognizedA = dependencies.find { it.name == 'unrecognized/a' }
        assert unrecognizedA.dependencies.isEmpty()
    }
}
