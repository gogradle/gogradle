package com.github.blindpirate.gogradle.core.dependency.tree;

import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Resolve all dependencies including transitive ones of a package and build a tree.
 * In this process, conflict may be resolved.
 */
@Singleton
public class DependencyTreeFactory {
    @Inject
    private DependencyRegistry registry;

    public DependencyTreeNode getTree(ResolvedDependency rootProject) {

        resolve(rootProject);

        return getSubTree(rootProject, new HashSet<>());
    }

    private DependencyTreeNode getSubTree(ResolvedDependency resolvedDependency,
                                          Set<ResolvedDependency> existedDependenciesInTree) {

        ResolvedDependency finalDependency = registry.retrive(resolvedDependency.getName());

        boolean hasExistedInTree = existedDependenciesInTree.contains(finalDependency);

        DependencyTreeNode node = DependencyTreeNode.withOrignalAndFinal(resolvedDependency,
                finalDependency,
                hasExistedInTree);

        if (!hasExistedInTree) {
            existedDependenciesInTree.add(finalDependency);
            for (GolangDependency dependency : finalDependency.getDependencies()) {
                node.addChild(getSubTree(dependency.resolve(), existedDependenciesInTree));
            }
        }
        return node;
    }

    private void resolve(ResolvedDependency resolvedDependency) {
        if (!registry.register(resolvedDependency)) {
            // current dependency is older
            return;
        }
        for (GolangDependency unresolvedChild : resolvedDependency.getDependencies()) {
            ResolvedDependency resolvedChild = unresolvedChild.resolve();
            resolve(resolvedChild);
        }
    }
}
