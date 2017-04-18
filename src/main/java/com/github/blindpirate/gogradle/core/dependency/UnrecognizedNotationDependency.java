package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.core.cache.CacheScope;
import com.github.blindpirate.gogradle.core.exceptions.UnrecognizedPackageException;

import java.util.Set;
import java.util.function.Predicate;

public class UnrecognizedNotationDependency extends AbstractGolangDependency implements NotationDependency {

    public static UnrecognizedNotationDependency of(UnrecognizedGolangPackage pkg) {
        UnrecognizedNotationDependency ret = new UnrecognizedNotationDependency();
        ret.setPackage(pkg);
        ret.setName(pkg.getPathString());
        return ret;
    }

    private UnrecognizedNotationDependency() {
    }

    @Override
    public boolean isFirstLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheScope getCacheScope() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Predicate<GolangDependency>> getTransitiveDepExclusions() {
        throw UnrecognizedPackageException.cannotRecognizePackage((UnrecognizedGolangPackage) getPackage());
    }

    @Override
    public ResolvedDependency resolve(ResolveContext context) {
        throw UnrecognizedPackageException.cannotRecognizePackage((UnrecognizedGolangPackage) getPackage());
    }
}
