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

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangConfiguration
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InOrder
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.asGolangDependencySet
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.isNull
import static org.mockito.Mockito.inOrder
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DependencyTreeFactoryTest {
    DependencyTreeFactory factory = new DependencyTreeFactory()

    @Mock
    DependencyRegistry registry

    @Mock
    ResolveContext context
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
            when(it.resolve(isNull())).thenReturn(it)
            when(it.resolve(any(ResolveContext))).thenReturn(it)
            when(it.getDependencies()).thenReturn(GolangDependencySet.empty())
            when(registry.register(it)).thenReturn(true)
        }

        DependencyTreeNode.metaClass.getChildren = {
            return ReflectionUtils.getField(delegate, 'children')
        }
        DependencyTreeNode.metaClass.getFinalDependency = {
            return ReflectionUtils.getField(delegate, 'finalDependency')
        }

        when(context.getDependencyRegistry()).thenReturn(registry)
        when(context.getConfiguration()).thenReturn(configuration)
        when(context.createSubContext(any(GolangDependency))).thenReturn(context)
        when(configuration.getDependencyRegistry()).thenReturn(registry)
    }

    void bind(ResolvedDependency dependency, String name) {
        when(dependency.getName()).thenReturn(name)
        when(registry.retrieve(name)).thenReturn(dependency)
    }

    void bindDependencies(ResolvedDependency dependency, GolangDependency... dependencies) {
        def set = asGolangDependencySet(dependencies)
        when(dependency.getDependencies()).thenReturn(set)
    }

    @Test
    void 'dependency conflict should be resolved'() {
        /*
        rootProject
            |-- a1 -> a2
            |   \\- c
            \\- b
                \\- a2
                    \\- d
                        \\- a3 -> a2

       the result is:

       rootProject
            |-- a2
            |   \\- d
            |       \\- a2 (*)
            \\- b
                \\- a2 (*)
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
        DependencyTreeNode rootNode = factory.getTree(context, rootProject)
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
        order.verify(a1).resolve(context)
        order.verify(b).resolve(context)
        order.verify(c).resolve(context)
        order.verify(a2).resolve(context)
        order.verify(d).resolve(context)
        order.verify(a3).resolve(context)
    }

    @Test
    void 'sub context should be created before resolution'() {
        'dependency conflict should be resolved'()
        InOrder order = inOrder(context, a1, a2, a3, b, c, d)
        order.verify(context).createSubContext(a1)
        order.verify(a1).resolve(context)
        order.verify(context).createSubContext(b)
        order.verify(b).resolve(context)
        order.verify(context).createSubContext(c)
        order.verify(c).resolve(context)
        order.verify(context).createSubContext(a2)
        order.verify(a2).resolve(context)
        order.verify(context).createSubContext(d)
        order.verify(d).resolve(context)
        order.verify(context).createSubContext(a3)
        order.verify(a3).resolve(context)
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if package is unrecognized'() {
        UnrecognizedGolangPackage pkg = UnrecognizedGolangPackage.of('unrecognized')
        bindDependencies(rootProject, UnrecognizedNotationDependency.of(pkg))
        factory.getTree(context, rootProject)
    }

    void assertChildrenOfNodeAre(DependencyTreeNode node, ResolvedDependency... expectedChildren) {
        List children = node.getChildren()
        assert children.size() == expectedChildren.size()
        children.each {
            assert expectedChildren.contains(it.getFinalDependency())
        }
    }
}
