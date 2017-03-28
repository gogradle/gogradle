package com.github.blindpirate.gogradle.core.dependency.tree

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InOrder
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.inOrder
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DependencyTreeFactoryTest {
    DependencyTreeFactory factory = new DependencyTreeFactory()

    @Mock
    DependencyRegistry registry

    @Mock
    GolangConfiguration configuration

    @Mock
    ResolvedDependency rootProject

    @Mock
    ResolvedDependency a1
    @Mock
    ResolvedDependency a2
    @Mock
    ResolvedDependency a3
    @Mock
    ResolvedDependency b
    @Mock
    ResolvedDependency c
    @Mock
    ResolvedDependency d

    @Before
    void setUp() {
        [rootProject, a1, a2, a3, b, c, d].each {
            when(it.resolve(configuration)).thenReturn(it)
            when(it.getDependencies()).thenReturn(GolangDependencySet.empty())
            when(registry.register(it)).thenReturn(true)
        }

        DependencyTreeNode.metaClass.getChildren = {
            return ReflectionUtils.getField(delegate, 'children')
        }
        DependencyTreeNode.metaClass.getFinalDependency = {
            return ReflectionUtils.getField(delegate, 'finalDependency')
        }

        when(configuration.getDependencyRegistry()).thenReturn(registry)
    }

    void bind(ResolvedDependency dependency, String name) {
        when(dependency.getName()).thenReturn(name)
        when(registry.retrieve(name)).thenReturn(dependency)
    }

    void bindDependencies(ResolvedDependency dependency, ResolvedDependency... dependencies) {
        def set = asGolangDependencySet(dependencies)
        when(dependency.getDependencies()).thenReturn(set)
    }

    @Test
    void 'dependency conflict should be resolved'() {
        /*
        rootProject
            ├── a1 -> a2
            │   └── c
            └── b
                └── a2
                    └── d
                        └── a3 -> a2

       the result is:

       rootProject
            ├── a2
            │   └── d
            │       └── a2 (*)
            └── b
                └── a2 (*)
         */
        // given
        // a2 is newer than a1 and a3
        when(a1.getName()).thenReturn('a')
        when(a3.getName()).thenReturn('a')
        when(registry.register(a3)).thenReturn(false)

        bind(rootProject, 'rootProject')
        bind(a2, 'a')
        bind(b, 'b')
        bind(c, 'c')
        bind(d, 'd')

        bindDependencies(rootProject, a1, b)
        bindDependencies(a1, c)
        bindDependencies(b, a2)
        bindDependencies(a2, d)
        bindDependencies(d, a3)
        // when
        DependencyTreeNode rootNode = factory.getTree(configuration, rootProject)
        // then
        assertChildrenOfNodeAre(rootNode, a2, b)
        assertChildrenOfNodeAre(rootNode.children[0], d)
        assertChildrenOfNodeAre(rootNode.children[1], a2)
        assertChildrenOfNodeAre(rootNode.children[0].children[0], a2)
    }

    @Test
    void 'resolution order should be BFS instead of DFS'() {
        'dependency conflict should be resolved'()
        InOrder order = inOrder(rootProject, a1, a2, a3, b, c, d)
        order.verify(a1).resolve(any(GolangConfiguration))
        order.verify(b).resolve(any(GolangConfiguration))
        order.verify(c).resolve(any(GolangConfiguration))
        order.verify(a2).resolve(any(GolangConfiguration))
        order.verify(d).resolve(any(GolangConfiguration))
        order.verify(a3).resolve(any(GolangConfiguration))
    }

    void assertChildrenOfNodeAre(DependencyTreeNode node, ResolvedDependency... expectedChildren) {
        List children = node.getChildren()
        assert children.size() == expectedChildren.size()
        children.each {
            assert expectedChildren.contains(it.getFinalDependency())
        }
    }
}
