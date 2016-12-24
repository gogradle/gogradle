package com.github.blindpirate.gogradle.core.dependency.produce

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.dependency.GolangConfiguration
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.LockedDependencyManager
import java.util.Optional
import org.gradle.api.artifacts.ConfigurationContainer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.core.mode.BuildMode.Develop
import static com.github.blindpirate.gogradle.core.mode.BuildMode.Reproducible
import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static com.github.blindpirate.gogradle.util.DependencyUtils.asOptional
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


    @Before
    void setUp() {
        strategy = new GogradleRootProduceStrategy(
                golangPluginSetting,
                configurationContainer,
                lockedDependencyManager)


        when(configurationContainer.getByName(anyString()))
                .thenReturn(configuration)
    }

    void dependenciesInBuildDotGradle(GolangDependency... dependencies) {
        GolangDependencySet result = asGolangDependencySet(dependencies)
        when(configuration.getDependencies()).thenReturn(result.toDependencySet())
    }

    void lockedDependencies(GolangDependency... dependencies) {
        Optional<GolangDependencySet> result = asOptional(dependencies)
        when(lockedDependencyManager.getLockedDependencies()).thenReturn(result)
    }

    @Test
    void 'dependencies in build.gradle should have top priority when in Develop mode'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(Develop)
        dependenciesInBuildDotGradle(a1, b1)
        lockedDependencies(a2)
        vendorDependencies(b2)

        // when
        def resultDependencies = strategy.produce(module, visitor)

        // then
        assert resultDependencies.any { it.is(a1) }
        assert resultDependencies.any { it.is(b1) }
        assert !resultDependencies.any { it.is(a2) }
        assert !resultDependencies.any { it.is(b2) }

    }

    @Test
    void 'dependencis in vendor should have top priority when in Reproducible mode'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(Reproducible)
        dependenciesInBuildDotGradle(a1)
        lockedDependencies(a2, b2)
        vendorDependencies()

        // when
        def result = strategy.produce(module, visitor)

        // then
        assert result.any { it.is(a2) }
        assert result.any { it.is(b2) }
        assert !result.any { it.is(a1) }
        assert !result.any { it.is(b1) }
    }

    @Test
    void 'locked dependencies should have priority than build.gradle in Reproducible mode'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(Reproducible)
        dependenciesInBuildDotGradle(a1, b1)
        lockedDependencies(a2)
        vendorDependencies()

        // when
        def result = strategy.produce(module, visitor)

        // then
        assert result.any { it.is(a2) }
        assert result.any { it.is(b1) }
        assert !result.any { it.is(a1) }
    }

    @Test
    void 'source code should be scanned when no dependencies exist'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(Develop)
        externalDependencies()
        dependenciesInBuildDotGradle()
        lockedDependencies()
        vendorDependencies()

        // when
        strategy.produce(module, visitor)

        // then
        verify(visitor).visitSourceCodeDependencies(module)
    }

    @Test
    void 'external tools should be scanned when no dependencies exist in build.gradle'() {
        // given
        when(golangPluginSetting.getBuildMode()).thenReturn(Develop)


        externalDependencies(a1)
        dependenciesInBuildDotGradle()
        lockedDependencies()
        vendorDependencies()

        // when
        def result = strategy.produce(module, visitor)

        // then
        assert result.any { it.is(a1) }
    }

}
