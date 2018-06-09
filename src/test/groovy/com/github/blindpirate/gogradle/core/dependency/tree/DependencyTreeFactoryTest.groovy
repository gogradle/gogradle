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
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException
import com.github.blindpirate.gogradle.core.exceptions.ResolutionStackWrappingException
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Ignore
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

    // a3 > a2 > a1, e2 > e1, f2 > f1
    DependencyRegistry registry = new MockDependencyRegistry()

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
    @Mock
    ResolvedDependency e1
    @Mock
    ResolvedDependency e2
    @Mock
    ResolvedDependency f1
    @Mock
    ResolvedDependency f2

    Map dependencyToNameMap = [:]

    @Before
    void setUp() {
        dependencyToNameMap[rootProject] = 'rootProject'
        dependencyToNameMap[a1] = 'a1'
        dependencyToNameMap[a2] = 'a2'
        dependencyToNameMap[a3] = 'a3'
        dependencyToNameMap[b] = 'b'
        dependencyToNameMap[c] = 'c'
        dependencyToNameMap[d] = 'd'
        dependencyToNameMap[e1] = 'e1'
        dependencyToNameMap[e2] = 'e2'
        dependencyToNameMap[f1] = 'f1'
        dependencyToNameMap[f2] = 'f2'

        context = ResolveContext.root(rootProject, configuration)

        [rootProject, a1, a2, a3, b, c, d, e1, e2, f1, f2].each {
            when(it.resolve(isNull())).thenReturn(it)
            when(it.resolve(any(ResolveContext))).thenReturn(it)
            when(it.getDependencies()).thenReturn(GolangDependencySet.empty())
            when(it.getName()).thenReturn(getDependencyName(dependencyToNameMap[it]))
        }

        DependencyTreeNode.metaClass.getChildren = {
            return ReflectionUtils.getField(delegate, 'children')
        }
        DependencyTreeNode.metaClass.getFinalDependency = {
            return ReflectionUtils.getField(delegate, 'finalDependency')
        }

        when(configuration.getDependencyRegistry()).thenReturn(registry)
    }


    void bindDependencies(ResolvedDependency dependency, GolangDependency... dependencies) {
        def set = asGolangDependencySet(dependencies)
        when(dependency.getDependencies()).thenReturn(set)
    }

    @Test
    void 'stacktrace should be properly formed on resolution failure'() {
        // given:
        bindDependencies(rootProject, b)
        bindDependencies(b, c)
        bindDependencies(c, d)
        when(d.resolve(any(ResolveContext))).thenThrow(DependencyResolutionException.cannotParseNotation('123456'))

        // when
        try {
            factory.getTree(context, rootProject)
            assert false
        }
        catch (ResolutionStackWrappingException e) {
            assert e.toString().contains('''
Cannot parse notation 123456
Resolution stack is:
+- rootProject
 +- b
  +- c
''')
        }
    }

    @Test
    void 'dependency conflict should be resolved'() {
        /*
        rootProject
            |-- a1 -> a3
            |   \- c
            \- b
                \- a3
                    \- d
                        \- a2 -> a3

       the result is:

       rootProject
            |-- a3
            |   \- d
            |       \- a3 (*)
            \- b
                \- a3 (*)
         */
        // given
        bindDependencies(rootProject, a1, b)
        bindDependencies(a1, c)
        bindDependencies(b, a3)
        bindDependencies(a3, d)
        bindDependencies(d, a2)
        // when
        DependencyTreeNode rootNode = factory.getTree(context, rootProject)
        // then
        assertChildrenOfNodeAre(rootNode, a3, b)
        assertChildrenOfNodeAre(rootNode.children[0], d)
        assertChildrenOfNodeAre(rootNode.children[1], a3)
        assertChildrenOfNodeAre(rootNode.children[0].children[0], a3)
    }

    @Test
    @Ignore("https://github.com/gogradle/gogradle/issues/207")
    void 'resolution order should be BFS instead of DFS'() {
        /*
    rootProject
        |-- b
        |   \- e2
        |     \- f1
        \- e1
            \- f2

    the result is:

    rootProject
        |-- b
        |   \- e2
        |       \- f1
        \- e1 -> e2
    */
        // given
        bindDependencies(rootProject, b, e1)
        bindDependencies(b, e2)
        bindDependencies(e2, f1)
        bindDependencies(e1, f2)

        // when
        DependencyTreeNode rootNode = factory.getTree(context, rootProject)

        // then
        assertChildrenOfNodeAre(rootNode, b, e2)
        assertChildrenOfNodeAre(rootNode.children[0], e2)
        assertChildrenOfNodeAre(rootNode.children[0].children[0], f1)

        InOrder order = inOrder(rootProject, b, e1, e2, f1, f2)
        order.verify(b).resolve(context)
        order.verify(e1).resolve(context)
        order.verify(e2).resolve(context)
        order.verify(f2).resolve(context)
        order.verify(f1).resolve(context)
    }

    @Test
    void 'sub context should be created before resolution'() {
        'dependency conflict should be resolved'()
        InOrder order = inOrder(a1, a2, a3, b, c, d)
        order.verify(a1).resolve(any(ResolveContext))
        order.verify(b).resolve(any(ResolveContext))
        order.verify(c).resolve(any(ResolveContext))
        order.verify(a3).resolve(any(ResolveContext))
        order.verify(d).resolve(any(ResolveContext))
        order.verify(a2).resolve(any(ResolveContext))
    }

    void assertChildrenOfNodeAre(DependencyTreeNode node, ResolvedDependency... expectedChildren) {
        List children = node.getChildren()
        assert children.size() == expectedChildren.size()
        children.each {
            assert expectedChildren.contains(it.getFinalDependency())
        }
    }


    class MockDependencyRegistry implements DependencyRegistry {
        Map<String, ResolvedDependency> registeredDependencies = [:]

        @Override
        boolean register(ResolvedDependency dependency) {
            String fieldName = dependencyToNameMap[dependency]
            String dependencyName = getDependencyName(fieldName)
            int dependencyVersion = getDependencyVersion(fieldName)

            String existingDependency = registeredDependencies[dependencyName]
            if (existingDependency == null) {
                registeredDependencies[dependencyName] = dependency
                return true
            } else {
                int existingVersion = getDependencyVersion(existingDependency)
                if (existingVersion < dependencyVersion) {
                    registeredDependencies[dependencyName] = dependency
                    return true
                } else {
                    return false
                }
            }
        }

        @Override
        Optional<ResolvedDependency> retrieve(String name) {
            return Optional.ofNullable(registeredDependencies[name])
        }
    }

    String getDependencyName(String name) {
        if (name == 'rootProject') {
            return name
        } else {
            return name.substring(0, 1)
        }
    }

    Integer getDependencyVersion(String name) {
        if (name == 'rootProject' || name.length() == 1) {
            return 1
        } else {
            return name.substring(1, 2).toInteger()
        }
    }
}
