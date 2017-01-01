package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import org.gradle.api.specs.Spec;

import java.util.Map;
import java.util.Set;

import static com.github.blindpirate.gogradle.core.InjectionHelper.INJECTOR_INSTANCE;

public abstract class AbstractNotationDependency extends AbstractGolangDependency implements NotationDependency {

    public static final String VERSION_KEY = "version";

    @Override
    public ResolvedDependency resolve() {
        DependencyResolver resolver = INJECTOR_INSTANCE.getInstance(this.resolverClass());
        return resolver.resolve(this);
    }

    protected abstract Class<? extends DependencyResolver> resolverClass();

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

    @Override
    public Set<Spec<GolangDependency>> getTransitiveDepExclusions() {
        return transitiveDepExclusions;
    }


}
