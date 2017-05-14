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

package com.github.blindpirate.gogradle.util

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Key
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Paths
import java.util.function.Function

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

class MockUtils {

    static ProjectCacheManager projectCacheManagerWithoutCache() {
        ProjectCacheManager ret = mock(ProjectCacheManager)
        when(ret.produce(any(ResolvedDependency), any(Function))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(1).apply(invocation.getArgument(0))
            }
        })

        when(ret.resolve(any(NotationDependency), any(Function))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(1).apply(invocation.getArgument(0))
            }
        })

        return ret
    }

    static void mockVcsService(Class serviceClass, Class annoClass, Object serviceInstance) {
        when(GogradleGlobal.INSTANCE.getInstance(Key.get(serviceClass, annoClass))).thenReturn(serviceInstance)
    }

    static Object mockMutipleInterfaces(Class... interfaceClasses) {
        Assert.isTrue(interfaceClasses.length > 0)
        if (interfaceClasses.length == 1) {
            return mock(interfaceClasses[0])
        } else {
            return mock(interfaceClasses[0], withSettings().extraInterfaces(interfaceClasses[1..-1] as Class[]))
        }
    }

    static VcsGolangPackage mockVcsPackage() {
        return VcsGolangPackage.builder()
                .withPath('github.com/user/package/a')
                .withRootPath('github.com/user/package')
                .withOriginalVcsInfo(VcsType.GIT, ['git@github.com:user/package.git', 'https://github.com/user/package.git'])
                .build()
    }

    static boolean isMockVcsPackage(VcsGolangPackage pkg) {
        return pkg.path == Paths.get('github.com/user/package/a') &&
                pkg.rootPath == Paths.get('github.com/user/package') &&
                pkg.originalVcsInfo.vcsType == VcsType.GIT &&
                pkg.originalVcsInfo.urls == ['git@github.com:user/package.git', 'https://github.com/user/package.git']
    }

    static VcsGolangPackage mockRootVcsPackage() {
        return VcsGolangPackage.builder()
                .withPath('github.com/user/package')
                .withRootPath('github.com/user/package')
                .withOriginalVcsInfo(VcsType.GIT, ['git@github.com:user/package.git', 'https://github.com/user/package.git'])
                .build()
    }

    static VcsGolangPackage mockSubstitutedRootVcsPackage() {
        return VcsGolangPackage.builder()
                .withPath('github.com/user/package')
                .withRootPath('github.com/user/package')
                .withSubstitutedVcsInfo(VcsType.GIT, ['git@github.com:user/package.git', 'https://github.com/user/package.git'])
                .build()
    }

    static DependencyProduceStrategy mockDependencyProduceStrategy() {
        return new DependencyProduceStrategy() {
            @Override
            GolangDependencySet produce(ResolvedDependency dependency, File rootDir, DependencyVisitor visitor, String configurationName) {
                return GolangDependencySet.empty()
            }
        }
    }
}
