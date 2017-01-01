package com.github.blindpirate.gogradle.core.dependency.tree;

import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import javax.inject.Inject;
import javax.inject.Singleton;

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
        return getSubTree(rootProject);
    }

    private DependencyTreeNode getSubTree(ResolvedDependency resolvedDependency) {
        ResolvedDependency finalDependency = registry.retrive(resolvedDependency.getName());

        DependencyTreeNode node = DependencyTreeNode.withOrignalAndFinal(resolvedDependency, finalDependency);

        for (GolangDependency dependency : finalDependency.getDependencies()) {
            node.addChild(getSubTree(dependency.resolve()));
        }
        return node;
    }

    private void resolve(ResolvedDependency resolvedDependency) {
        for (GolangDependency unresolvedDependency : resolvedDependency.getDependencies()) {
            ResolvedDependency dependency = unresolvedDependency.resolve();
            if (registry.register(dependency)) {
                // current dependency is the newest
                resolve(dependency);
            }
        }
    }
}
