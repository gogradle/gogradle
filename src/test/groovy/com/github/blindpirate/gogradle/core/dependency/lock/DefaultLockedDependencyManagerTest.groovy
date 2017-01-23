package com.github.blindpirate.gogradle.core.dependency.lock

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.WithProject
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static com.github.blindpirate.gogradle.build.Configuration.BUILD
import static com.github.blindpirate.gogradle.build.Configuration.TEST
import static com.github.blindpirate.gogradle.util.MockUtils.mockMutipleInterfaces
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithProject
class DefaultLockedDependencyManagerTest {
    DefaultLockedDependencyManager manager
    Project project
    @Mock
    MapNotationParser parser
    GolangDependency dependency1 = mockMutipleInterfaces(NotationDependency, ResolvedDependency)
    GolangDependency dependency2 = mockMutipleInterfaces(NotationDependency, ResolvedDependency)
    GolangDependency dependency3 = mockMutipleInterfaces(NotationDependency, ResolvedDependency)
    GolangDependency dependency4 = mockMutipleInterfaces(NotationDependency, ResolvedDependency)

    String LOCK_FILE_NAME = 'gogradle.lock'
    String warning = ReflectionUtils.getStaticField(DefaultLockedDependencyManager, "WARNING")
    String gogradleDotLock =
            """${warning}---
apiVersion: "${GogradleGlobal.GOGRADLE_VERSION}"
dependencies:
  build:
  - name: "a"
    version: "v1"
  - name: "b"
    version: "v2"
  test:
  - name: "a"
    version: "v2"
  - name: "c"
    version: "v3"
"""

    @Before
    void setUp() {
        manager = new DefaultLockedDependencyManager(parser, project)
        when(dependency1.getName()).thenReturn('a')
        when(dependency2.getName()).thenReturn('b')
        when(dependency3.getName()).thenReturn('a')
        when(dependency4.getName()).thenReturn('c')
    }

    void prepareGogradleDotLock() {
        when(parser.parse(eq([name: 'a', version: 'v1']))).thenReturn(dependency1)
        when(parser.parse(eq([name: 'b', version: 'v2']))).thenReturn(dependency2)
        when(parser.parse(eq([name: 'a', version: 'v2']))).thenReturn(dependency3)
        when(parser.parse(eq([name: 'c', version: 'v3']))).thenReturn(dependency4)
        IOUtils.write(project.getRootDir(), LOCK_FILE_NAME, gogradleDotLock)
    }

    @Test
    void 'reading from gogradle.lock should succeed'() {
        // given
        prepareGogradleDotLock()
        // when
        GolangDependencySet buildResult = manager.getLockedDependencies(BUILD)
        GolangDependencySet testResult = manager.getLockedDependencies(TEST)
        // then
        assert buildResult.any { it.is(dependency1) }
        assert buildResult.any { it.is(dependency2) }
        assert testResult.any { it.is(dependency3) }
        assert testResult.any { it.is(dependency4) }
    }

    @Test
    @WithResource('')
    void 'reading other gogradle project\'s dependencies should succeed'() {
        // given
        prepareGogradleDotLock()
        // when
        GolangDependencySet buildResult = manager.produce(project.rootDir, BUILD).get()
        GolangDependencySet testResult = manager.produce(project.rootDir, TEST).get()
        // then
        assert buildResult.any { it.is(dependency1) }
        assert buildResult.any { it.is(dependency2) }
        assert testResult.any { it.is(dependency3) }
        assert testResult.any { it.is(dependency4) }
    }

    @Test
    void 'writing to gogradle.lock should succeed'() {
        // given
        when(dependency1.toLockedNotation()).thenReturn([name: 'a', version: 'v1'])
        when(dependency2.toLockedNotation()).thenReturn([name: 'b', version: 'v2'])
        when(dependency3.toLockedNotation()).thenReturn([name: 'a', version: 'v2'])
        when(dependency4.toLockedNotation()).thenReturn([name: 'c', version: 'v3'])

        // when
        manager.lock([dependency1, dependency2], [dependency3, dependency4])
        // then
        assert new File(project.getRootDir(), LOCK_FILE_NAME).getText() == gogradleDotLock
    }

    @Test
    void 'existent gogradle.lock should be overwritten'() {
        IOUtils.write(project.getRootDir(), LOCK_FILE_NAME, 'old file content')
        'writing to gogradle.lock should succeed'()
    }

    @Test
    void 'empty dependencies should be returned if gogradle.lock does not exist'() {
        assert manager.getLockedDependencies().isEmpty()
    }
}

