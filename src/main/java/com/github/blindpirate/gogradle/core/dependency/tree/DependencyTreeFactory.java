package com.github.blindpirate.gogradle.core.dependency.tree;

import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Resolve all dependencies including transitive ones of a package and build a tree.
 * In this process, conflict may be resolved.
 */
@Singleton
public class DependencyTreeFactory {
    public DependencyTreeNode getTree(GolangConfiguration configuration, ResolvedDependency rootProject) {
        resolve(configuration, rootProject);
        return getSubTree(configuration, rootProject, new HashSet<>());
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
                node.addChild(getSubTree(configuration, dependency.resolve(configuration), existedDependenciesInTree));
            }
        }
        return node;
    }

    private void resolve(GolangConfiguration configuration, ResolvedDependency resolvedDependency) {
        if (!configuration.getDependencyRegistry().register(resolvedDependency)) {
            // current dependency is older
            return;
        }
        for (GolangDependency unresolvedChild : resolvedDependency.getDependencies()) {
            ResolvedDependency resolvedChild = unresolvedChild.resolve(configuration);
            resolve(configuration, resolvedChild);
        }
    }
}
