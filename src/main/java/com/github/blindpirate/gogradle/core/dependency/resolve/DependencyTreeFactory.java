package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyTree;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
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

    public DependencyTree getTree(GolangPackageModule module) {

        if (registry.shouldBeIgnore(module)) {
            return null;
        }

        GolangDependencySet dependencies = factory.produce(module);
        DependencyTree ret = new DependencyTree(module);

        // can be run concurrently
        for (Dependency dependency : dependencies) {
            GolangPackageModule backingModule = ((GolangDependency) dependency).getPackage();
            DependencyTree childNode = getTree(backingModule);

            if (childNode != null) {
                ret.addChild(childNode);
            }
        }

        return ret;
    }

}
