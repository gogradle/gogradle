package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class DefaultDependencyRegistry implements DependencyRegistry {
    private Map<String, GolangPackageModule> packages = new HashMap<>();

    @Override
    public boolean register(GolangPackageModule module) {
        synchronized (packages) {
            GolangPackageModule existingModule = packages.get(module.getName());
            if (existingModule != null && theyAreAllFirstLevel(existingModule, module)) {
                throw new IllegalStateException("First-level package " + module.getName() + " conflict!");
            } else if (module.isFirstLevel()
                    || existingModuleIsOutOfDate(existingModule, module)) {
                packages.put(module.getName(), module);
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean existingModuleIsOutOfDate(GolangPackageModule existingModule, GolangPackageModule module) {
        if (existingModule == null) {
            return true;
        }
        return existingModule.getUpdateTime() < module.getUpdateTime();
    }

    private boolean theyAreAllFirstLevel(GolangPackageModule existedModule, GolangPackageModule module) {
        return existedModule.isFirstLevel() && module.isFirstLevel();
    }
}
