package com.github.blindpirate.gogradle.core.dependency;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DefaultDependencyRegistry implements DependencyRegistry {

    private Map<String, ResolvedDependency> packages = new HashMap<>();

    private ConcurrentHashMap<NotationDependency, ResolvedDependency> cache = new ConcurrentHashMap<>();

    @Override
    public boolean register(ResolvedDependency resolvedDependency) {
        synchronized (packages) {
            ResolvedDependency existent = packages.get(resolvedDependency.getName());
            if (existent == null) {
                return registerSucceed(resolvedDependency);
            } else if (theyAreAllFirstLevel(existent, resolvedDependency)) {
                throw new IllegalStateException("First-level package " + resolvedDependency.getName()
                        + " conflict!");
            } else if (existent.isFirstLevel()) {
                return false;
            } else if (resolvedDependency.isFirstLevel()) {
                return registerSucceed(resolvedDependency);
            } else if (existentDependencyIsOutOfDate(existent, resolvedDependency)) {
                return registerSucceed(resolvedDependency);
            } else {
                return false;
            }
        }
    }

    private boolean registerSucceed(ResolvedDependency resolvedDependency) {
        packages.put(resolvedDependency.getName(), resolvedDependency);
        return true;
    }

    @Override
    public ResolvedDependency retrive(String name) {
        return packages.get(name);
    }

    @Override
    public Optional<ResolvedDependency> getFromCache(NotationDependency dependency) {
        return Optional.ofNullable(cache.get(dependency));
    }

    @Override
    public void putIntoCache(NotationDependency dependency, ResolvedDependency resolvedDependency) {
        cache.put(dependency, resolvedDependency);
    }

    private boolean existentDependencyIsOutOfDate(ResolvedDependency existingDependency,
                                                  ResolvedDependency resolvedDependency) {
        if (existingDependency == null) {
            return true;
        }
        return existingDependency.getUpdateTime() < resolvedDependency.getUpdateTime();
    }

    private boolean theyAreAllFirstLevel(ResolvedDependency existedModule, ResolvedDependency resolvedDependency) {
        return existedModule.isFirstLevel() && resolvedDependency.isFirstLevel();
    }
}
