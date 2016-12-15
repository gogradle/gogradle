package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyTreeNode;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.google.common.base.Optional;
import org.gradle.api.artifacts.Dependency;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Resolve all dependencies including transitive ones of a package.
 */
@Singleton
public class DependencyTreeFactory {
    @Inject
    private DependencyFactory factory;
    @Inject
    private DependencyRegistry registry;

    public DependencyTreeNode getTree(GolangPackageModule module) {
        return getSubTreeRootNode(module).get();
    }

    private Optional<DependencyTreeNode> getSubTreeRootNode(GolangPackageModule module) {
        if (!registry.register(module)) {
            return Optional.absent();
        }

        GolangDependencySet dependencies = factory.produce(module);
        DependencyTreeNode ret = new DependencyTreeNode(module);

        // can be run concurrently
        for (Dependency dependency : dependencies) {
            GolangPackageModule backingModule = ((GolangDependency) dependency).getPackage();
            Optional<DependencyTreeNode> childNode = getSubTreeRootNode(backingModule);

            if (childNode.isPresent()) {
                ret.addChild(childNode.get());
            }
        }

        return Optional.of(ret);
    }

}
