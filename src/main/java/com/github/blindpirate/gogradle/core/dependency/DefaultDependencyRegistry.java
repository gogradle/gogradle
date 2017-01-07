package com.github.blindpirate.gogradle.core.dependency;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DefaultDependencyRegistry implements DependencyRegistry {
    private Map<String, ResolvedDependency> packages = new HashMap<>();

    @Override
    public boolean register(ResolvedDependency resolvedDependency) {
        synchronized (packages) {
            ResolvedDependency existent = packages.get(resolvedDependency.getName());
            if (existent != null && theyAreAllFirstLevel(existent, resolvedDependency)) {
                throw new IllegalStateException("First-level package " + resolvedDependency.getName()
                        + " conflict!");
            } else if (resolvedDependency.isFirstLevel()
                    || existingDependencyIsOutOfDate(existent, resolvedDependency)) {
                packages.put(resolvedDependency.getName(), resolvedDependency);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public ResolvedDependency retrive(String name) {
        return packages.get(name);
    }

    private boolean existingDependencyIsOutOfDate(ResolvedDependency existingDependency,
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
