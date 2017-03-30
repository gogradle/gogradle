package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.google.inject.Key
import org.gradle.api.specs.Spec
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

@RunWith(GogradleRunner)
class VcsResolvedDependencyTest {
    @Mock
    NotationDependency notationDependency
    @Mock
    Spec exclusionSpec

    @Before
    void setUp() {
        Mockito.when(notationDependency.getTransitiveDepExclusions()).thenReturn([exclusionSpec] as Set)
    }

    VcsResolvedDependency newResolvedDependency() {
        return VcsResolvedDependency.builder(VcsType.GIT)
                .withName("package")
                .withCommitId('commitId')
                .withCommitTime(42L)
                .withUrl('url')
                .withNotationDependency(notationDependency)
                .build()
    }

    @Test
    void 'updateTime should be read correctly'() {
        assert newResolvedDependency().getUpdateTime() == 42L
    }

    @Test
    void 'a resolved dependency should be converted to notation successfully'() {
        // given
        VcsResolvedDependency dependency = newResolvedDependency()
        // then
        assert dependency.toLockedNotation() == [name: 'package', commit: 'commitId', vcs: 'git', url: 'url']
    }

    @Test
    void 'notation\'s specs should be copied into resolved dependency'() {
        // given
        VcsResolvedDependency dependency = newResolvedDependency()
        assert DependencyUtils.getExclusionSpecs(dependency).contains(exclusionSpec)
    }

    @Test
    @WithMockInjector
    void 'getInstallerClass() should succeed'() {
        DependencyInstaller installer = Mockito.mock(DependencyInstaller)
        Mockito.when(GogradleGlobal.INSTANCE.getInjector().getInstance(Key.get(DependencyInstaller, Git))).thenReturn(installer)
        assert newResolvedDependency().installer == installer
    }

    @Test
    void 'formatting should succeed'() {
        assert newResolvedDependency().formatVersion() == 'commitI'
    }

    @Test
    void 'formatting with tag should succeed'() {
        // given
        VcsResolvedDependency dependency = newResolvedDependency()
        ReflectionUtils.setField(dependency, 'tag', 'tag')

        // then
        assert dependency.formatVersion() == 'tag(commitI)'
    }

    @Test
    void 'dependencies should be equal if name/version/url equals'() {
        def dependency1 = newResolvedDependency()
        def dependency2 = newResolvedDependency()

        assert dependency1 == dependency2

        ReflectionUtils.setField(dependency1, 'tag', 'tag1')
        ReflectionUtils.setField(dependency2, 'tag', 'tag2')
        ReflectionUtils.setField(dependency1, 'updateTime', 0L)
        ReflectionUtils.setField(dependency2, 'updateTime', 1L)
        dependency1.firstLevel = true
        dependency2.firstLevel = false
        assert dependency1 == dependency2
    }
}
