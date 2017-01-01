package com.github.blindpirate.gogradle.core.dependency.tree

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DependencyTreeFactoryTest {
    @InjectMocks
    DependencyTreeFactory factory

    @Mock
    DependencyRegistry registry

    @Mock
    ResolvedDependency rootProject

    @Mock
    ResolvedDependency a1
    @Mock
    ResolvedDependency a2
    @Mock
    ResolvedDependency b
    @Mock
    ResolvedDependency c
    @Mock
    ResolvedDependency d


    @Before
    void setUp() {
        [a1, a2, b, c, d].each {
            when(it.resolve()).thenReturn(it)
            when(it.getDependencies()).thenReturn(GolangDependencySet.empty())
            when(registry.register(it)).thenReturn(true)
        }


        DependencyTreeNode.metaClass.getChildren = {
            return ReflectionUtils.getField(delegate, 'children')
        }
        DependencyTreeNode.metaClass.getFinalDependency = {
            return ReflectionUtils.getField(delegate, 'finalDependency')
        }
    }

    void bind(ResolvedDependency dependency, String name) {
        when(dependency.getName()).thenReturn(name)
        when(registry.retrive(name)).thenReturn(dependency)
    }

    void bindDependencies(ResolvedDependency dependency, ResolvedDependency... dependencies) {
        def set = asGolangDependencySet(dependencies)
        when(dependency.getDependencies()).thenReturn(set)
    }

    @Test
    void 'dependency conflict should be resolved'() {
        /*
        └── a1
            ├── a1 -> a2
            │   └── c
            └── b
                └── a2
                    └── d
         */
        // given
        when(a1.getName()).thenReturn('a')
        bind(rootProject, 'rootProject')
        bind(a2, 'a')
        bind(b, 'b')
        bind(c, 'c')
        bind(d, 'd')

        bindDependencies(rootProject, a1, b)
        bindDependencies(a1, c)
        bindDependencies(b, a2)
        bindDependencies(a2, d)
        // when
        DependencyTreeNode rootNode = factory.getTree(rootProject)
        // then
        assertChildrenOfNodeAre(rootNode, a2, b)
        assertChildrenOfNodeAre(rootNode.getChildren()[0], d)
        assertChildrenOfNodeAre(rootNode.getChildren()[1], a2)
    }

    void assertChildrenOfNodeAre(DependencyTreeNode node, ResolvedDependency... expectedChildren) {
        List children = node.getChildren()
        assert children.size() == expectedChildren.size()
        children.each {
            assert expectedChildren.contains(it.getFinalDependency())
        }
    }
}
