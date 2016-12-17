package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.pack.DependencyResolver;
import org.omg.CORBA.Object;

import java.util.HashMap;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.dependency.DependencyHelper.INJECTOR_INSTANCE;

public abstract class AbstractNotationDependency extends AbstractGolangDependency {

    private boolean transitive = true;

    private Map<String, Object> excludes = new HashMap<>();

    @Override
    public GolangPackageModule getPackage() {
        DependencyResolver resolver = INJECTOR_INSTANCE.getInstance(this.resolverClass());
        return resolver.resolve(this);
    }

    protected abstract Class<? extends DependencyResolver> resolverClass();

    public boolean isTransitive() {
        return transitive;
    }

    public Map<String, Object> getExcludes() {
        return excludes;
    }

    public void exclude(Map<String, Object> map) {
        this.excludes.putAll(map);
    }

    public AbstractNotationDependency setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException();
    }

}
