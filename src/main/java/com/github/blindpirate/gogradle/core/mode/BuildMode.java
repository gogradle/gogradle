package com.github.blindpirate.gogradle.core.mode;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import static com.github.blindpirate.gogradle.core.dependency.GolangDependencySet.merge;

public enum BuildMode {
    DEVELOP {
        @Override
        public GolangDependencySet determine(GolangDependencySet declaredDependencies,
                                             GolangDependencySet vendorDependencies,
                                             GolangDependencySet lockedDependencies) {
            GolangDependencySet declaredAndLocked = merge(declaredDependencies, lockedDependencies);

            vendorDependencies.flatten()
                    .stream()
                    .map(dependency -> (ResolvedDependency) dependency)
                    .forEach(dependency -> dependency.getDependencies().removeAll(declaredAndLocked));

            return merge(declaredAndLocked, vendorDependencies);
        }
    },
    REPRODUCIBLE {
        @Override
        public GolangDependencySet determine(GolangDependencySet declaredDependencies,
                                             GolangDependencySet vendorDependencies,
                                             GolangDependencySet lockedDependencies) {
            GolangDependencySet lockedAndDeclared = merge(lockedDependencies, declaredDependencies);

            lockedAndDeclared.removeAll(vendorDependencies.flatten());

            return merge(vendorDependencies, lockedAndDeclared);
        }
    };

    public abstract GolangDependencySet determine(
            GolangDependencySet declaredDependencies,
            GolangDependencySet vendorDependencies,
            GolangDependencySet lockedDependencies);
}
