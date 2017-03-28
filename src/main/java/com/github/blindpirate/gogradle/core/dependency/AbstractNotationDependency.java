package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

import java.util.Map;

/**
 * All implementations must override equals() and hashCode()
 */
public abstract class AbstractNotationDependency extends AbstractGolangDependency implements NotationDependency {

    public static final String VERSION_KEY = "version";

    private ResolvedDependency resolvedDependency;

    private GolangPackage golangPackage;

    public GolangPackage getPackage() {
        return golangPackage;
    }

    public void setPackage(GolangPackage golangPackage) {
        this.golangPackage = golangPackage;
    }

    @Override
    public ResolvedDependency resolve(GolangConfiguration configuration) {
        if (resolvedDependency == null) {
            resolvedDependency = doResolve(configuration);
        }
        return resolvedDependency;
    }

    protected ResolvedDependency doResolve(GolangConfiguration configuration) {
        DependencyResolver resolver = GogradleGlobal.getInstance(this.getResolverClass());
        return resolver.resolve(configuration, this);
    }

    protected abstract Class<? extends DependencyResolver> getResolverClass();

    public void exclude(Map<String, Object> map) {
        transitiveDepExclusions.add(PropertiesExclusionSpec.of(map));
    }

    public void setTransitive(boolean transitive) {
        if (transitive) {
            transitiveDepExclusions.remove(NO_TRANSITIVE_DEP_SPEC);
        } else {
            transitiveDepExclusions.add(NO_TRANSITIVE_DEP_SPEC);
        }
    }

}
