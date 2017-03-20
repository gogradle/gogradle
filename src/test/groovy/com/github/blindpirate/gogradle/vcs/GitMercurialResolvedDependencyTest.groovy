package com.github.blindpirate.gogradle.vcs

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager
import org.gradle.api.specs.Spec
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

@RunWith(GogradleRunner)
class GitMercurialResolvedDependencyTest {
    @Mock
    NotationDependency notationDependency
    @Mock
    Spec exclusionSpec

    @Before
    void setUp() {
        Mockito.when(notationDependency.getTransitiveDepExclusions()).thenReturn([exclusionSpec] as Set)
    }

    GitMercurialResolvedDependency newResolvedDependency() {
        return GitMercurialResolvedDependency.gitBuilder()
                .withName("package")
                .withCommitId('commitId')
                .withCommitTime(42L)
                .withRepoUrl('repoUrl')
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
        GitMercurialResolvedDependency dependency = newResolvedDependency()
        // then
        assert dependency.toLockedNotation() == [name: 'package', commit: 'commitId', vcs: 'git', url: 'repoUrl']
    }

    @Test
    void 'notation\'s specs should be copied into resolved dependency'() {
        // given
        GitMercurialResolvedDependency dependency = newResolvedDependency()
        assert DependencyUtils.getExclusionSpecs(dependency).contains(exclusionSpec)
    }

    @Test
    void 'getInstallerClass() should succeed'() {
        assert newResolvedDependency().installerClass == GitDependencyManager
    }

    @Test
    void 'formatting should succeed'() {
        assert newResolvedDependency().formatVersion() == 'commitI'
    }

    @Test
    void 'formatting with tag should succeed'() {
        // given
        GitMercurialResolvedDependency dependency = newResolvedDependency()
        ReflectionUtils.setField(dependency, 'tag', 'tag')

        // then
        assert dependency.formatVersion() == 'tag(commitI)'
    }
}
