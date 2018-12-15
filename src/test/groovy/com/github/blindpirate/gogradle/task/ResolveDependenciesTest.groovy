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

package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.support.MockRefreshDependencies
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.internal.tasks.TaskStateInternal
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Path

import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.NO_TRANSITIVE_DEP_PREDICATE
import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.PropertiesExclusionPredicate
import static com.github.blindpirate.gogradle.core.mode.BuildMode.DEVELOP
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Matchers.any
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class ResolveDependenciesTest extends TaskTest {
    ResolveBuildDependencies resolveBuildDependenciesTask
    ResolveTestDependencies resolveTestDependenciesTask

    File resource
    @Mock
    DependencyTreeNode tree
    @Mock
    Path rootPath
    @Mock
    GolangConfiguration configuration
    @Mock
    DependencyVisitor dependencyVisitor

    GogradleRootProject gogradleRootProject

    @Before
    void setUp() {
        when(project.getProjectDir()).thenReturn(resource)
        when(rootPath.toFile()).thenReturn(resource)
        when(setting.getPackagePath()).thenReturn("package")

        resolveBuildDependenciesTask = buildTask(ResolveBuildDependencies)
        resolveTestDependenciesTask = buildTask(ResolveTestDependencies)

        gogradleRootProject = new GogradleRootProject(project)
        gogradleRootProject.setName('package')
        ReflectionUtils.setField(resolveBuildDependenciesTask, 'gogradleRootProject', gogradleRootProject)
        ReflectionUtils.setField(resolveTestDependenciesTask, 'gogradleRootProject', gogradleRootProject)

        when(configurationManager.getByName(anyString())).thenReturn(configuration)
        when(strategy.produce(any(ResolvedDependency), any(File), any(DependencyVisitor), anyString())).thenReturn(GolangDependencySet.empty())

        when(GogradleGlobal.INSTANCE.getInjector().getInstance(DependencyVisitor)).thenReturn(dependencyVisitor)
        when(dependencyTreeFactory.getTree(any(ResolveContext), any(LocalDirectoryDependency))).thenReturn(tree)

        DependencyTreeNode.metaClass.getName = { ReflectionUtils.getField(delegate, 'name') }
        DependencyTreeNode.metaClass.getStar = { ReflectionUtils.getField(delegate, 'star') }
        DependencyTreeNode.metaClass.getOriginalDependency = {
            ReflectionUtils.getField(delegate, 'originalDependency')
        }
        DependencyTreeNode.metaClass.getFinalDependency = { ReflectionUtils.getField(delegate, 'finalDependency') }
        DependencyTreeNode.metaClass.getChildren = { ReflectionUtils.getField(delegate, 'children') }
    }

    @Test
    void 'dependency resolution should succeed'() {
        // when
        when(configuration.getName()).thenReturn('build')
        when(dependencyTreeFactory.getTree(any(ResolveContext), any(ResolvedDependency)))
                .thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                return buildDependencyTree(invocation.getArgument(1))
            }
        })
        resolveBuildDependenciesTask.resolve()
        // then
        assert new File(resource, ".gogradle/cache/build-${GogradleGlobal.GOGRADLE_COMPATIBLE_VERSION}.bin").exists()
        verify(projectCacheManager).loadPersistenceCache()
        verify(projectCacheManager).savePersistenceCache()
    }

    DependencyTreeNode buildDependencyTree(ResolvedDependency rootProject) {
        ResolvedDependency d1 = LocalDirectoryDependency.fromLocal('d1', resource)
        d1.exclude([name: 'excluded'])

        ResolvedDependency d2 = LocalDirectoryDependency.fromLocal('d2', resource)
        NotationDependency d3 = new GitNotationDependency()
        d3.name = 'd3'
        d3.commit = 'commit'
        d3.transitive = false
        d3.package = MockUtils.mockRootVcsPackage()

        ResolvedDependency d3Resolved = VcsResolvedDependency.builder()
                .withCommitId('commit')
                .withNotationDependency(d3)
                .build()
        ReflectionUtils.setField(d3, 'resolvedDependency', d3Resolved)

        d1.dependencies.add(d3)

        DependencyTreeNode root = DependencyTreeNode.withOriginalAndFinal(rootProject, rootProject, false)
        DependencyTreeNode node1 = DependencyTreeNode.withOriginalAndFinal(d1, d1, false)
        DependencyTreeNode node2 = DependencyTreeNode.withOriginalAndFinal(d2, d2, false)
        DependencyTreeNode node3 = DependencyTreeNode.withOriginalAndFinal(d3Resolved, d3Resolved, true)
        root.addChild(node1)
        root.addChild(node2)
        node1.addChild(node3)
        return root
    }

    @Test
    void 'recovering from serialization file should succeed when task is up-to-date'() {
        // given
        'dependency resolution should succeed'()
        ReflectionUtils.setField(resolveBuildDependenciesTask, 'dependencyTree', null)
        TaskStateInternal state = ReflectionUtils.getField(resolveBuildDependenciesTask, 'state')
        state.outcome = TaskExecutionOutcome.UP_TO_DATE

        // when
        DependencyTreeNode tree = resolveBuildDependenciesTask.dependencyTree

        // then
        assert resolveBuildDependenciesTask.flatDependencies.size() == 3
        assert resolveBuildDependenciesTask.flatDependencies.collect { it.name } as Set == ['d1', 'd2', 'd3'] as Set

        assert tree.name == 'package'
        assert tree.children.size() == 2

        assertTreeNodeIs(tree.children[0], 'd1')
        assert tree.children[0].originalDependency.transitiveDepExclusions.size() == 1
        assert tree.children[0].originalDependency.dependencies.size() == 1
        assert tree.children[0].originalDependency.transitiveDepExclusions[0] == PropertiesExclusionPredicate.of([name: 'excluded'])

        GitNotationDependency d3NotationDependency = tree.children[0].originalDependency.dependencies.first()
        assert d3NotationDependency.name == 'd3'
        assert d3NotationDependency.commit == 'commit'
        assert d3NotationDependency.urls == ['git@github.com:user/package.git', 'https://github.com/user/package.git']
        assert d3NotationDependency.transitiveDepExclusions == [NO_TRANSITIVE_DEP_PREDICATE] as Set
        VcsResolvedDependency d3ResolvedDependency = ReflectionUtils.getField(d3NotationDependency, 'resolvedDependency')
        assert d3ResolvedDependency.name == 'd3'
        assert d3ResolvedDependency.version == 'commit'
        assert d3ResolvedDependency.urls == ['git@github.com:user/package.git', 'https://github.com/user/package.git']


        assertTreeNodeIs(tree.children[1], 'd2')

        assertTreeNodeIs(tree.children[0].children[0], 'd3')
        assert tree.children[0].children[0].originalDependency.is(d3ResolvedDependency)
    }

    @Test
    void 'dependencyTree should be null if that task is not executed at all'() {
        // given
        'dependency resolution should succeed'()
        ReflectionUtils.setField(resolveBuildDependenciesTask, 'dependencyTree', null)
        // then
        assert resolveBuildDependenciesTask.dependencyTree == null
        assert resolveBuildDependenciesTask.flatDependencies.isEmpty()
    }

    void assertTreeNodeIs(DependencyTreeNode node, String name) {
        assert node.name == name
        assert node.originalDependency instanceof ResolvedDependency
        assert node.originalDependency.is(node.finalDependency)
    }

    @Test
    void 'cache binary file should be updated even if error occurred'() {
        when(configurationManager.getByName(anyString())).thenThrow(RuntimeException)
        try {
            resolveBuildDependenciesTask.resolve()
        }
        catch (Exception e) {
            verify(projectCacheManager).savePersistenceCache()
        }
    }

    @Test
    void 'checking external files should succeed'() {
        assert resolveBuildDependenciesTask.externalLockfiles == new File(resource, 'gogradle.lock')
    }

    @Test
    void 'resolve task should depend on preprare task'() {
        assertTaskDependsOn(resolveBuildDependenciesTask, PREPARE_TASK_NAME)
        assertTaskDependsOn(resolveTestDependenciesTask, PREPARE_TASK_NAME)
    }

    @Test
    void 'checking dependencies should succeed'() {
        GolangDependencySet set = GolangDependencySet.empty()
        set.add(LocalDirectoryDependency.fromLocal('local', resource))

        when(configurationManager.getByName('build')).thenReturn(configuration)
        when(configuration.getDependencies()).thenReturn(set)
        assert resolveBuildDependenciesTask.getDependencies().containsAll(configuration.getDependencies())
    }

    @Test
    void 'checking build tags should succeed'() {
        when(setting.getBuildTags()).thenReturn([''])
        assert resolveBuildDependenciesTask.buildTags == ['']
    }

    @Test
    void 'checking dir dependencies should succeed'() {
        GolangDependencySet set = GolangDependencySet.empty()
        LocalDirectoryDependency local = LocalDirectoryDependency.fromLocal('local', resource)
        set.add(local)

        when(configurationManager.getByName('build')).thenReturn(configuration)
        when(configuration.getDependencies()).thenReturn(set)
        assert resolveBuildDependenciesTask.localDirDependencies == [resource]
    }

    @Test
    void 'checking buildMode should succeed'() {
        when(setting.getBuildMode()).thenReturn(DEVELOP)
        assert resolveBuildDependenciesTask.buildMode == 'DEVELOP'
    }

    @Test
    @MockRefreshDependencies(true)
    void 'refreshDependenciesFlag should vary every time if --refresh-dependencies is set'() {
        assert resolveBuildDependenciesTask.refreshDependenciesFlag != resolveBuildDependenciesTask.refreshDependenciesFlag
        assert resolveBuildDependenciesTask.refreshDependenciesFlag != resolveBuildDependenciesTask.refreshDependenciesFlag
    }

    @Test
    @MockRefreshDependencies(false)
    void 'refreshDependenciesFlag should be stable every time if --refresh-dependencies not set'() {
        assert resolveBuildDependenciesTask.refreshDependenciesFlag == resolveBuildDependenciesTask.refreshDependenciesFlag
        assert resolveBuildDependenciesTask.refreshDependenciesFlag == resolveBuildDependenciesTask.refreshDependenciesFlag
    }

    @Test
    void 'checking go source files should succeed'() {
        IOUtils.write(resource, 'main.go', '')
        IOUtils.write(resource, 'main_test.go', '')
        assert resolveBuildDependenciesTask.goSourceFiles == [new File(resource, 'main.go')]
        assert resolveTestDependenciesTask.goSourceFiles == [new File(resource, 'main_test.go')]
    }
}
