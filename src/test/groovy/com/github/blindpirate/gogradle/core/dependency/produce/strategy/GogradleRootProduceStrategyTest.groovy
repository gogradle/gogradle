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

package com.github.blindpirate.gogradle.core.dependency.produce.strategy

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.GolangConfigurationManager
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.lock.LockedDependencyManager
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.core.mode.BuildMode.DEVELOP
import static com.github.blindpirate.gogradle.core.mode.BuildMode.REPRODUCIBLE
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class GogradleRootProduceStrategyTest extends DependencyProduceStrategyTest {

    GogradleRootProduceStrategy strategy
    @Mock
    GolangPluginSetting golangPluginSetting
    @Mock
    GolangConfigurationManager configurationManager
    @Mock
    GolangConfiguration configuration
    @Mock
    Project project
    @Mock
    LockedDependencyManager lockedDependencyManager

    @Before
    void setUp() {
        strategy = new GogradleRootProduceStrategy(
                golangPluginSetting,
                configurationManager,
                lockedDependencyManager)
        when(configurationManager.getByName(anyString()))
                .thenReturn(configuration)
        when(lockedDependencyManager.canRecognize(rootDir)).thenReturn(true)
    }

    void dependenciesInBuildDotGradle(GolangDependency... dependencies) {
        GolangDependencySet result = asGolangDependencySet(dependencies)
        when(configuration.getDependencies()).thenReturn(result)
    }

    void assertResultIs(GolangDependencySet actual, List expected) {
        assert actual.size() == expected.size()
        assert expected.intersect(actual) == expected
    }

    @Test
    void 'dependencies in build.gradle should be used in DEVELOP mode'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(DEVELOP)
        when(lockedDependencyManager.canRecognize(rootDir)).thenReturn(false)
        dependenciesInBuildDotGradle(a1, b1)
        lockedDependencies(a2)
        vendorDependencies(b2)

        // when
        def resultDependencies = strategy.produce(resolvedDependency, rootDir, visitor, 'build')

        // then
        assertResultIs(resultDependencies, [a1, b1])
    }

    @Test
    void 'dependencies in build.gradle should be used when gogradle.lock does not exist'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(REPRODUCIBLE)
        when(lockedDependencyManager.canRecognize(rootDir)).thenReturn(false)
        dependenciesInBuildDotGradle(a1, b1)
        lockedDependencies()
        vendorDependencies(b2)

        // when
        def resultDependencies = strategy.produce(resolvedDependency, rootDir, visitor, 'build')

        // then
        assertResultIs(resultDependencies, [a1, b1])
    }

    @Test
    void 'locked dependencies should be used in REPRODUCIBLE mode'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(REPRODUCIBLE)
        when(lockedDependencyManager.canRecognize(rootDir)).thenReturn(true)
        dependenciesInBuildDotGradle(a1, b1)
        lockedDependencies(a2)
        vendorDependencies(b2)

        // when
        def resultDependencies = strategy.produce(resolvedDependency, rootDir, visitor, 'build')

        // then
        assertResultIs(resultDependencies, [a2])
    }

    @Test
    void 'dependencies in vendor should be ignored'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(REPRODUCIBLE)
        dependenciesInBuildDotGradle(a2)
        lockedDependencies(b2)
        vendorDependencies(a1, b1)

        // when
        assert strategy.produce(resolvedDependency, rootDir, visitor, 'build').disjoint([a1, b1])
    }

    @Test
    void 'source code should be scanned when no dependencies exist'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(DEVELOP)
        lockedDependencies()
        dependenciesInBuildDotGradle()
        vendorDependencies()
        sourceCodeDependencies()

        // when
        assert strategy.produce(resolvedDependency, rootDir, visitor, 'build').isEmpty()

        // then
        verify(visitor).visitSourceCodeDependencies(resolvedDependency, rootDir, 'build')
    }

    void lockedDependencies(GolangDependency... dependencies) {
        GolangDependencySet set = asGolangDependencySet(dependencies)
        when(lockedDependencyManager.produce(resolvedDependency, rootDir, 'build')).thenReturn(set)
        when(lockedDependencyManager.produce(resolvedDependency, rootDir, 'test')).thenReturn(set)
    }

}
