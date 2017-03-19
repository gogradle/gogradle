package com.github.blindpirate.gogradle.core.dependency.produce.strategy

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.lock.LockedDependencyManager
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
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
    LockedDependencyManager lockedDependencyManager
    @Mock
    ConfigurationContainer configurationContainer
    @Mock
    GolangConfiguration configuration
    @Mock
    Project project


    @Before
    void setUp() {
        when(project.getConfigurations()).thenReturn(configurationContainer)

        strategy = new GogradleRootProduceStrategy(
                golangPluginSetting,
                project,
                lockedDependencyManager)
        when(configurationContainer.getByName(anyString()))
                .thenReturn(configuration)
    }

    void dependenciesInBuildDotGradle(GolangDependency... dependencies) {
        GolangDependencySet result = asGolangDependencySet(dependencies)
        when(configuration.getDependencies()).thenReturn(result.toDependencySet())
    }

    void lockedDependencies(GolangDependency... dependencies) {
        GolangDependencySet result = asGolangDependencySet(dependencies)
        when(lockedDependencyManager.getLockedDependencies('build')).thenReturn(result)
    }

    @Test
    void 'dependencies in build.gradle should have top priority when in Develop mode'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(DEVELOP)
        dependenciesInBuildDotGradle(a1, b1)
        lockedDependencies(a2)
        vendorDependencies(b2)

        // when
        def resultDependencies = strategy.produce(resolvedDependency, rootDir, visitor, 'build')

        // then
        assert resultDependencies.any { it.is(a1) }
        assert resultDependencies.any { it.is(b1) }
        assert !resultDependencies.any { it.is(a2) }
        assert !resultDependencies.any { it.is(b2) }

    }

    @Test
    void 'dependencis in vendor should have top priority when in Reproducible mode'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(REPRODUCIBLE)
        dependenciesInBuildDotGradle(a1)
        lockedDependencies(a2, b2)
        vendorDependencies()

        // when
        def result = strategy.produce(resolvedDependency, rootDir, visitor, 'build')

        // then
        assert result.any { it.is(a2) }
        assert result.any { it.is(b2) }
        assert !result.any { it.is(a1) }
        assert !result.any { it.is(b1) }
    }

    @Test
    void 'locked dependencies should have priority than build.gradle in Reproducible mode'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(REPRODUCIBLE)
        dependenciesInBuildDotGradle(a1, b1)
        lockedDependencies(a2)
        vendorDependencies()

        // when
        def result = strategy.produce(resolvedDependency, rootDir, visitor, 'build')

        // then
        assert result.any { it.is(a2) }
        assert result.any { it.is(b1) }
        assert !result.any { it.is(a1) }
    }

    @Test
    void 'source code should be scanned when no dependencies exist'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(DEVELOP)
        externalDependencies()
        dependenciesInBuildDotGradle()
        lockedDependencies()
        vendorDependencies()

        // when
        strategy.produce(resolvedDependency, rootDir, visitor, 'build')

        // then
        verify(visitor).visitSourceCodeDependencies(resolvedDependency, rootDir, 'build')
    }

    @Test
    void 'external tools should be scanned when no dependencies exist in build.gradle'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(DEVELOP)


        externalDependencies(a1)
        dependenciesInBuildDotGradle()
        lockedDependencies()
        vendorDependencies()

        // when
        def result = strategy.produce(resolvedDependency, rootDir, visitor, 'build')

        // then
        assert result.any { it.is(a1) }
    }

}
