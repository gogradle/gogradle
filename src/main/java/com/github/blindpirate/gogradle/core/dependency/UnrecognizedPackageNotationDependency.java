package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import org.gradle.api.specs.Spec;

import java.util.Set;

public class UnrecognizedPackageNotationDependency extends AbstractGolangDependency implements NotationDependency {

    private UnrecognizedGolangPackage pkg;

    public static UnrecognizedPackageNotationDependency of(UnrecognizedGolangPackage pkg) {
        UnrecognizedPackageNotationDependency ret = new UnrecognizedPackageNotationDependency();
        ret.pkg = pkg;
        ret.setName(pkg.getPathString());
        return ret;
    }

    private UnrecognizedPackageNotationDependency() {
    }

    @Override
    public boolean isFirstLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GolangPackage getPackage() {
        return pkg;
    }

    @Override
    public Set<Spec<GolangDependency>> getTransitiveDepExclusions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResolvedDependency resolve(GolangConfiguration configuration) {
        DependencyRegistry registry = GogradleGlobal.getInstance(DependencyRegistry.class);
        ResolvedDependency resolvedDependency = registry.retrieve(getName());
        if (resolvedDependency != null) {
            return resolvedDependency;
        } else {
            throw new UnsupportedOperationException("Cannot resolve package: " + getName());
        }
    }
}
