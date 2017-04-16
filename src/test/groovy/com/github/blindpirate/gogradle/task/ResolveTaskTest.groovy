package com.github.blindpirate.gogradle.task

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.core.dependency.lock.DefaultLockedDependencyManager
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor
import com.github.blindpirate.gogradle.core.dependency.produce.external.glide.GlideDependencyFactory
import com.github.blindpirate.gogradle.core.dependency.produce.external.glock.GlockDependencyFactory
import com.github.blindpirate.gogradle.core.dependency.produce.external.godep.GodepDependencyFactory
import com.github.blindpirate.gogradle.core.dependency.produce.external.gopm.GopmDependencyFactory
import com.github.blindpirate.gogradle.core.dependency.produce.external.govendor.GovendorDependencyFactory
import com.github.blindpirate.gogradle.core.dependency.produce.external.gvtgbvendor.GvtGbvendorDependencyFactory
import com.github.blindpirate.gogradle.core.dependency.produce.external.trash.TrashDependencyFactory
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency
import com.github.blindpirate.gogradle.vcs.git.GitResolvedDependency
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Path

import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.NO_TRANSITIVE_DEP_PREDICATE
import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.PropertiesExclusionPredicate
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Matchers.any
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class ResolveTaskTest extends TaskTest {
    ResolveBuildDependenciesTask resolveBuildDependenciesTask
    ResolveTestDependenciesTask resolveTestDependenciesTask

    File resource
    @Mock
    DependencyTreeNode tree
    @Mock
    Path rootPath
    @Mock
    GolangConfiguration configuration
    @Mock
    DependencyVisitor dependencyVisitor

    @Before
    void setUp() {
        resolveBuildDependenciesTask = buildTask(ResolveBuildDependenciesTask)
        resolveTestDependenciesTask = buildTask(ResolveTestDependenciesTask)

        when(configurationManager.getByName(anyString())).thenReturn(configuration)
        when(strategy.produce(any(ResolvedDependency), any(File), any(DependencyVisitor), anyString())).thenReturn(GolangDependencySet.empty())

        when(rootPath.toFile()).thenReturn(resource)
        when(setting.getPackagePath()).thenReturn("package")
        when(project.getRootDir()).thenReturn(resource)
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
        assert new File(resource, '.gogradle/cache/build.bin').exists()
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
        d3.url = 'url'
        d3.transitive = false

        ResolvedDependency d3Resolved = GitResolvedDependency.builder(VcsType.GIT)
                .withName('d3')
                .withCommitId('commit')
                .withUrl('url')
                .withNotationDependency(d3)
                .build()
        ReflectionUtils.setField(d3, 'resolvedDependency', d3Resolved)

        d1.dependencies.add(d3)

        DependencyTreeNode root = DependencyTreeNode.withOrignalAndFinal(rootProject, rootProject, false)
        DependencyTreeNode node1 = DependencyTreeNode.withOrignalAndFinal(d1, d1, false)
        DependencyTreeNode node2 = DependencyTreeNode.withOrignalAndFinal(d2, d2, false)
        DependencyTreeNode node3 = DependencyTreeNode.withOrignalAndFinal(d3Resolved, d3Resolved, true)
        root.addChild(node1)
        root.addChild(node2)
        node1.addChild(node3)
        return root
    }

    @Test
    void 'recovering from serialization file should succeed'() {
        'dependency resolution should succeed'()
        ReflectionUtils.setField(resolveBuildDependenciesTask, 'dependencyTree', null)
        DependencyTreeNode tree = resolveBuildDependenciesTask.dependencyTree

        assert tree.name == 'package'
        assert tree.children.size() == 2

        assertTreeNodeIs(tree.children[0], 'd1')
        assert tree.children[0].originalDependency.transitiveDepExclusions.size() == 1
        assert tree.children[0].originalDependency.dependencies.size() == 1
        assert tree.children[0].originalDependency.transitiveDepExclusions[0] == PropertiesExclusionPredicate.of([name: 'excluded'])

        GitNotationDependency d3NotationDependency = tree.children[0].originalDependency.dependencies.first()
        assert d3NotationDependency.name == 'd3'
        assert d3NotationDependency.commit == 'commit'
        assert d3NotationDependency.urls == ['url']
        assert d3NotationDependency.transitiveDepExclusions == [NO_TRANSITIVE_DEP_PREDICATE] as Set
        GitResolvedDependency d3ResolvedDependency = ReflectionUtils.getField(d3NotationDependency, 'resolvedDependency')
        assert d3ResolvedDependency.name == 'd3'
        assert d3ResolvedDependency.version == 'commit'
        assert d3ResolvedDependency.url == 'url'


        assertTreeNodeIs(tree.children[1], 'd2')

        assertTreeNodeIs(tree.children[0].children[0], 'd3')
        assert tree.children[0].children[0].originalDependency.is(d3ResolvedDependency)
    }

    void assertTreeNodeIs(DependencyTreeNode node, String name) {
        assert node.name == name
        assert node.originalDependency instanceof ResolvedDependency
        assert node.originalDependency.is(node.finalDependency)
    }

    @Test
    void 'checking external files should succeed'() {
        ReflectionUtils.setField(resolveBuildDependenciesTask, 'externalDependencyFactories',
                [DefaultLockedDependencyManager,
                 GodepDependencyFactory,
                 GlideDependencyFactory,
                 GovendorDependencyFactory,
                 GvtGbvendorDependencyFactory,
                 TrashDependencyFactory,
                 GlockDependencyFactory,
                 GopmDependencyFactory].collect { it.newInstance() })
        ['gogradle.lock', 'glide.lock', 'vendor/vendor.json'].each {
            IOUtils.write(resource, it, '')
        }

        List<File> externalLockFiles = resolveBuildDependenciesTask.getExternalLockfiles()

        assert externalLockFiles.size() == 3
        assert externalLockFiles.any { it.name == 'gogradle.lock' }
        assert externalLockFiles.any { it.name == 'glide.lock' }
        assert externalLockFiles.any { it.name == 'vendor.json' }
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
    void 'checking vendor should succeed'() {
        IOUtils.mkdir(resource, 'vendor')
        assert resolveBuildDependenciesTask.vendorDirectory == [new File(resource, 'vendor')]
    }

    @Test
    void 'checking vendor should succeed if vendor not exist'() {
        assert resolveBuildDependenciesTask.vendorDirectory == []
    }

    @Test
    void 'checking build tags should succeed'() {
        when(setting.getBuildTags()).thenReturn([''])
        assert resolveBuildDependenciesTask.buildTags == ['']
    }

    @Test
    void 'checking go source files should succeed'() {
        IOUtils.write(resource, 'main.go', '')
        IOUtils.write(resource, 'main_test.go', '')
        assert resolveBuildDependenciesTask.goSourceFiles == [new File(resource, 'main.go')]
        assert resolveTestDependenciesTask.goSourceFiles == [new File(resource, 'main_test.go')]
    }
}
