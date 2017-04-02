package com.github.blindpirate.gogradle.core.dependency.tree;

import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                .retrieve(resolvedDependency.getName());

        boolean hasExistedInTree = existedDependenciesInTree.contains(finalDependency);

        DependencyTreeNode node = DependencyTreeNode.withOrignalAndFinal(resolvedDependency,
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

    private void resolve(ResolvedDependency resolvedDependency, ResolveContext context) {
        if (!context.getDependencyRegistry().register(resolvedDependency)) {
            // current dependency is older
            return;
        }

        // BFS order
        List<ResolvedDependency> resolvedDependencies = resolvedDependency.getDependencies()
                .stream()
                .map(dependency -> dependency.resolve(context))
                .collect(Collectors.toList());

        resolvedDependencies
                .forEach(dependency -> resolve(dependency, context.createSubContext(dependency)));

    }
}
