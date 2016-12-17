package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.google.common.base.Optional;

public interface ModuleDependencyVistor {

    /**
     * Visits dependencies managed by a external package manage tool such as godep, govendor, etc.
     * Usually, it's determined by analyzing "lock file" of that tool.
     *
     * @param module
     * @return Dependencies if external package manage tool detected, otherwise {@code Optional.empty()}.
     */
    Optional<GolangDependencySet> visitExternalDependencies(GolangPackageModule module);

    /**
     * Visits dependencies in vendor and treats them as file dependencies.
     *
     * @param module
     * @return Dependencies if .go file in vendor detected, otherwise {@code Optional.empty()}
     */
    Optional<GolangDependencySet> visitVendorDependencies(GolangPackageModule module);

    /**
     * Analyze the imports in source code (all .go files in root directory except vendor) to get dependencies.
     *
     * @param module
     * @return All imported package.
     */
    GolangDependencySet visitSourceCodeDependencies(GolangPackageModule module);
}
