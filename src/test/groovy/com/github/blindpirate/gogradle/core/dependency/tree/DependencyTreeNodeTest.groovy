package com.github.blindpirate.gogradle.core.dependency.tree

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import org.junit.Test

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class DependencyTreeNodeTest {

    @Test
    void 'tree building should success'() {
        // given
        def root = node('a')

        def _1 = node('b')
        def _2 = node('c')
        def _3 = node('d')

        def _2_1 = node('e')
        def _3_1 = node('f')
        def _3_2 = node('g')

        root.addChild(_1).addChild(_2).addChild(_3)
        _2.addChild(_2_1)
        _3.addChild(_3_1).addChild(_3_2)

        println(root.output())
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

    DependencyTreeNode node(String name) {
        ResolvedDependency dependency = mock(ResolvedDependency)
        when(dependency.getName()).thenReturn(name)
        return DependencyTreeNode.withOrignalAndFinal(dependency, dependency)
    }
}
