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

package com.github.blindpirate.gogradle.core.dependency.tree;

import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.exceptions.ResolutionStackWrappingException;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Resolve all dependencies including transitive ones of a package and build a tree.
 * In this process, conflict may be resolved.
 */
@Singleton
public class DependencyTreeFactory {
    public DependencyTreeNode getTree(ResolveContext context, ResolvedDependency rootProject) {
        resolve(rootProject, context);
        return getSubTree(context.getConfiguration(), rootProject, new HashSet<>());
    }

    private DependencyTreeNode getSubTree(GolangConfiguration configuration,
                                          ResolvedDependency resolvedDependency,
                                          Set<ResolvedDependency> existedDependenciesInTree) {
        ResolvedDependency finalDependency = configuration.getDependencyRegistry()
                .retrieve(resolvedDependency.getName()).get();

        boolean hasExistedInTree = existedDependenciesInTree.contains(finalDependency);

        DependencyTreeNode node = DependencyTreeNode.withOriginalAndFinal(resolvedDependency,
                finalDependency,
                hasExistedInTree);

        if (!hasExistedInTree) {
            existedDependenciesInTree.add(finalDependency);
            for (GolangDependency dependency : finalDependency.getDependencies()) {
                // 'cause it has been cached in AbstractNotationDependency
                node.addChild(getSubTree(configuration, dependency.resolve(null), existedDependenciesInTree));
            }
        }
        return node;
    }

    private void resolve(ResolvedDependency rootProject, ResolveContext context) {
        // BFS order
        Queue<ResolveOperation> queue = new LinkedList<>();
        queue.add(new ResolveOperation(rootProject, context));
        while (!queue.isEmpty()) {
            ResolveOperation item = queue.remove();
            if (context.getDependencyRegistry().register(item.dependencyToPropagate)) {
                try {
                    item.dependencyToPropagate
                            .getDependencies()
                            .stream()
                            .map(dep -> createSubContextAndResolve(dep, item.context))
                            .forEach(queue::add);
                } catch (Throwable e) {
                    throw ResolutionStackWrappingException.wrapWithResolutionStack(e, item.context);
                }
            }
        }
    }

    private ResolveOperation createSubContextAndResolve(GolangDependency dependency,
                                                        ResolveContext parentContext) {
        ResolveContext subContext = parentContext.createSubContext(dependency);
        ResolvedDependency resolvedDependency = dependency.resolve(subContext);
        return new ResolveOperation(resolvedDependency, subContext);
    }

    private static class ResolveOperation {
        private ResolvedDependency dependencyToPropagate;
        private ResolveContext context;

        private ResolveOperation(ResolvedDependency dependencyToPropagate, ResolveContext context) {
            this.dependencyToPropagate = dependencyToPropagate;
            this.context = context;
        }
    }
}
