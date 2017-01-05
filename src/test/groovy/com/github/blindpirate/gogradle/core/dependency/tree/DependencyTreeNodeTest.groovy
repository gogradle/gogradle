package com.github.blindpirate.gogradle.core.dependency.tree

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import org.junit.Before
import org.junit.Test

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class DependencyTreeNodeTest {

    DependencyTreeNode root

    @Before
    void setUp() {
        root = node('a')

        def _1 = node('b')
        def _2 = node('c')
        def _3 = node('d')

        def _2_1 = node('e')
        def _3_1 = node('f')
        def _3_2 = node('g')

        root.addChild(_1).addChild(_2).addChild(_3)
        _2.addChild(_2_1)
        _3.addChild(_3_1).addChild(_3_2)
    }

    @Test
    void 'tree building should success'() {
        // then
        assert root.output() == '''\
a
├── b √
├── c √
│   └── e √
└── d √
    ├── f √
    └── g √
'''
    }

    @Test
    void 'flattening a tree should success'() {
        // when
        GolangDependencySet result = root.flatten()
        assert result.size() == 6
        assert ('b'..'g').intersect(result.collect({ it.name })) == ('b'..'g')
    }

    DependencyTreeNode node(String name) {
        ResolvedDependency dependency = mock(ResolvedDependency)
        when(dependency.getName()).thenReturn(name)
        return DependencyTreeNode.withOrignalAndFinal(dependency, dependency)
    }
}
