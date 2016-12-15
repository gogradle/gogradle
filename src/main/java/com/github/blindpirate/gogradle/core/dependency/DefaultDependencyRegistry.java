package com.github.blindpirate.gogradle.core.dependency;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DefaultDependencyRegistry implements DependencyRegistry {
    private Map<String, GolangDependency> packages = new HashMap<>();

    @Override
    public boolean registry(GolangDependency dependency) {
        synchronized (packages) {
            GolangDependency existingDependency = packages.get(dependency.getName());
            if (existingDependency != null && theyAreAllFirstLevel(existingDependency, dependency)) {
                throw new IllegalStateException("First-level package " + dependency.getName() + " conflict!");
            } else if (dependency.isFirstLevel() ||
                    existingDependencyIsOutOfDate(existingDependency, dependency)) {
                packages.put(dependency.getName(), dependency);
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean existingDependencyIsOutOfDate(GolangDependency existingDependency, GolangDependency dependency) {
        if (existingDependency == null) {
            return true;
        }
        return existingDependency.getPackage().getUpdateTime()
                < dependency.getPackage().getUpdateTime();
    }

    private boolean theyAreAllFirstLevel(GolangDependency existedDependency, GolangDependency dependency) {
        return existedDependency.isFirstLevel() && dependency.isFirstLevel();
    }
}
