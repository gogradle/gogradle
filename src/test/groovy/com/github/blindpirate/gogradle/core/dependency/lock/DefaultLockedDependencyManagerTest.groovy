package com.github.blindpirate.gogradle.core.dependency.lock

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.WithProject
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser
import com.github.blindpirate.gogradle.core.dependency.produce.external.ExternalDependencyFactoryTest
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.MockUtils.mockMutipleInterfaces
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.when

// TODO what if gogradle.lock already exists?
@RunWith(GogradleRunner)
@WithProject
class DefaultLockedDependencyManagerTest {
    DefaultLockedDependencyManager manager
    Project project
    @Mock
    MapNotationParser parser
    GolangDependency dependency1 = mockMutipleInterfaces(NotationDependency, ResolvedDependency)
    GolangDependency dependency2 = mockMutipleInterfaces(NotationDependency, ResolvedDependency)

    String LOCK_FILE_NAME = 'gogradle.lock'
    String warning = ReflectionUtils.getStaticField(DefaultLockedDependencyManager, "WARNING")
    String gogradleDotLock =
            """${warning}---
apiVersion: "${GolangPluginSetting.GOGRADLE_VERSION}"
dependencies:
- name: "a"
  version: "v1"
- name: "b"
  version: "v2"
"""

    @Before
    void setUp() {
        manager = new DefaultLockedDependencyManager(parser, project)
        when(dependency1.getName()).thenReturn('a')
        when(dependency2.getName()).thenReturn('b')
    }

    void prepareGogradleDotLock() {
        when(parser.parse(eq([name: 'a', version: 'v1']))).thenReturn(dependency1)
        when(parser.parse(eq([name: 'b', version: 'v2']))).thenReturn(dependency2)
        IOUtils.write(project.getRootDir(), LOCK_FILE_NAME, gogradleDotLock)
    }

    @Test
    void 'reading from gogradle.lock should succeed'() {
        // given
        prepareGogradleDotLock()
        // when
        GolangDependencySet result = manager.getLockedDependencies()
        // then
        assert result.any { it.is(dependency1) }
        assert result.any { it.is(dependency2) }
    }

    @Test
    @WithResource('')
    void 'reading other gogradle project\'s dependencies should succeed'() {
        // given
        prepareGogradleDotLock()
        // when
        GolangDependencySet result = manager.produce(project.rootDir).get()
        // then
        assert result.any { it.is(dependency1) }
        assert result.any { it.is(dependency2) }
    }

    @Test
    void 'writing to gogradle.lock should succeed'() {
        // given
        when(dependency1.toLockedNotation()).thenReturn([name: 'a', version: 'v1'])
        when(dependency2.toLockedNotation()).thenReturn([name: 'b', version: 'v2'])

        // when
        manager.lock([dependency1, dependency2])
        // then
        assert project.getRootDir().toPath().resolve(LOCK_FILE_NAME).toFile().getText() == gogradleDotLock
    }

    @Test
    void 'empty dependencies should be returned if gogradle.lock does not exist'() {
        assert manager.getLockedDependencies().isEmpty()
    }
}

