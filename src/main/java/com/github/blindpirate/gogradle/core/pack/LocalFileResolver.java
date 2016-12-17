package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;

import java.nio.file.Paths;

public class LocalFileResolver implements DependencyResolver {
    @Override
    public GolangPackageModule resolve(GolangDependency dependency) {
        LocalDirectoryDependency directoryDependency = (LocalDirectoryDependency) dependency;
        return new LocalFileSystemModule(directoryDependency.getName(),
                Paths.get(directoryDependency.getPath()));
    }
}
