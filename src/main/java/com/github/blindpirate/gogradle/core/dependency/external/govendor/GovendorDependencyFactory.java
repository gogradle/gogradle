package com.github.blindpirate.gogradle.core.dependency.external.govendor;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.resolve.ExternalDependencyFactory;

import java.util.List;
import java.util.Optional;

/**
 * vendor/vendor.json
 */
class GovendorDependencyFactory extends ExternalDependencyFactory {
    @Override
    protected List<String> identityFiles() {
        return null;
    }

    @Override
    protected Optional<GolangDependencySet> doProduce(GolangPackageModule module) {
        return Optional.empty();
    }
}
