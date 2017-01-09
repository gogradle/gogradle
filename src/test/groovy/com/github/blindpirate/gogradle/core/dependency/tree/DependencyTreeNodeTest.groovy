package com.github.blindpirate.gogradle.core.dependency.tree

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.util.DependencyUtils
import org.junit.Before
import org.junit.Test

class DependencyTreeNodeTest {

    DependencyTreeNode root

    @Before
    void setUp() {
        root = node('a')

        def _1 = node('b', 'b', true)
        def _2 = node('c', 'c', false)
        def _3 = node('d')

        def _2_1 = node('e')
        def _3_1 = node('f', 'f', true)
        def _3_2 = node('g')

        root.addChild(_1).addChild(_2).addChild(_3)
        _2.addChild(_2_1)
        _3.addChild(_3_1).addChild(_3_2)
    }

    @Test
    void 'tree building whth check mark should succeed'() {
        // then
        assert root.output() == '''\
a
├── b -> b (*)
├── c -> c
│   └── e √
└── d √
    ├── f -> f (*)
    └── g √
'''
    }

    @Test
    void 'flattening a tree should succeed'() {
        // when
        GolangDependencySet result = root.flatten()
        assert result.size() == 6
        assert ('b'..'g').intersect(result.collect({ it.name })) == ('b'..'g')
    }


    DependencyTreeNode node(String originalName, String finalName, boolean star) {
        ResolvedDependency original = DependencyUtils.mockResolvedDependency(originalName)
        ResolvedDependency _final = DependencyUtils.mockResolvedDependency(finalName)
        return DependencyTreeNode.withOrignalAndFinal(original, _final, star)
    }

    DependencyTreeNode node(String name) {
        ResolvedDependency dependency = DependencyUtils.mockResolvedDependency(name)
        return DependencyTreeNode.withOrignalAndFinal(dependency, dependency, false)
    }
}
