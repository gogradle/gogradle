package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.core.exceptions.UnrecognizedPackageException;

import java.util.Set;
import java.util.function.Predicate;

public class UnrecognizedPackageNotationDependency extends AbstractGolangDependency implements NotationDependency {

    public static UnrecognizedPackageNotationDependency of(UnrecognizedGolangPackage pkg) {
        UnrecognizedPackageNotationDependency ret = new UnrecognizedPackageNotationDependency();
        ret.setPackage(pkg);
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
    public Set<Predicate<GolangDependency>> getTransitiveDepExclusions() {
        throw UnrecognizedPackageException.cannotRecognizePackage((UnrecognizedGolangPackage) getPackage());
    }

    @Override
    public ResolvedDependency resolve(ResolveContext context) {
        throw UnrecognizedPackageException.cannotRecognizePackage((UnrecognizedGolangPackage) getPackage());
    }
}
