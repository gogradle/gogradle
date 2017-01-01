package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

import java.util.Map;

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
        addTransitiveDepExclusion(PropertiesExclusionSpec.of(map));
    }

    public void setTransitive(boolean transitive) {
        if (transitive) {
            remoteTransitiveDepExclusion(NO_TRANSITIVE_DEP_SPEC);
        } else {
            addTransitiveDepExclusion(NO_TRANSITIVE_DEP_SPEC);
        }
    }


}
