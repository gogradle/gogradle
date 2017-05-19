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

package com.github.blindpirate.gogradle.core.dependency.tree

import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager
import com.github.blindpirate.gogradle.util.DependencyUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import groovy.transform.EqualsAndHashCode
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class DependencyTreeNodeTest {

    DependencyTreeNode root

    @Before
    void setUp() {
        root = node('a')

        def _1 = node('b', 'b', true)
        def _2 = node('c', 'c', false)
        def _3 = node('d')

        def _2_1 = node('e')
        def _2_1_1 = node('h')
        def _3_1 = node('f', 'f', true)
        def _3_2 = node('g')

        root.addChild(_3).addChild(_2).addChild(_1)
        _2.addChild(_2_1)
        _2_1.addChild(_2_1_1)
        _3.addChild(_3_1).addChild(_3_2)
    }

    @Test
    void 'tree building whth check mark should succeed'() {
        println(root.output())
        assert root.output() == '''\
a
|-- b:version -> version (*)
|-- c:version -> version
|   \\-- e:version
|       \\-- h:version
\\-- d:version
    |-- f:version -> version (*)
    \\-- g:version
'''
    }

    @Test
    void 'flattening a tree should succeed'() {
        // when
        GolangDependencySet result = root.flatten()
        assert result.size() == 7
        assert ('b'..'g').intersect(result.collect({ it.name })) == ('b'..'g')
    }

    @Test
    void 'two equal node should be marked as a same dependency'() {
        // given
        ResolvedDependency b1 = withNameAndVersion('b', 'version')
        ResolvedDependency b2 = withNameAndVersion('b', 'version')
        ReflectionUtils.setField(root, 'children', [DependencyTreeNode.withOrignalAndFinal(b1, b2, false)])
        // then
        assert root.output() == '''\
a
\\-- b:version
'''
    }

    @Test
    void 'subpackages should be printed'() {
        // given
        ResolvedDependency b1 = withNameAndVersion('b', 'version1')
        ResolvedDependency b2 = withNameAndVersion('b', 'version2')

        b1.subpackages = ['sub1', 'sub2']

        ReflectionUtils.setField(root, 'children', [DependencyTreeNode.withOrignalAndFinal(b1, b2, false)])
        // then
        assert root.output() == '''\
a
\\-- b:version1 [sub1, sub2] -> version2
'''
    }

    @Test
    void 'subpackages should be printed2'() {
        // given
        ResolvedDependency b1 = withNameAndVersion('b', 'version1')

        b1.subpackages = ['sub1', 'sub2']

        ReflectionUtils.setField(root, 'children', [DependencyTreeNode.withOrignalAndFinal(b1, b1, true)])
        // then
        assert root.output() == '''\
a
\\-- b:version1 [sub1, sub2] (*)
'''
    }

    ResolvedDependency withNameAndVersion(String name, String version) {
        return new Temp(name, version, 0L)
    }

    static class Temp extends AbstractResolvedDependency {
        private static final int serialVersionUID = 1

        protected Temp(String name, String version, long updateTime) {
            super(name, version, updateTime)
        }

        @Override
        protected DependencyManager getInstaller() {
            return null
        }

        @Override
        Map<String, Object> toLockedNotation() {
            return null
        }

        @Override
        String formatVersion() {
            return getVersion()
        }
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if max def depth reached'() {
        DependencyTreeNode node = node('a')
        node.addChild(node)
        node.flatten()
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
