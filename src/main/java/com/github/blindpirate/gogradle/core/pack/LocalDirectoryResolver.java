package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.IOUtils;

import java.io.File;

import static com.github.blindpirate.gogradle.util.Assert.isNotNull;

public class LocalDirectoryResolver implements DependencyResolver {
    @Override
    public ResolvedDependency resolve(NotationDependency dependency) {
        LocalDirectoryNotationDependency directoryDependency = (LocalDirectoryNotationDependency) dependency;
        File rootDir = new File(isNotNull(directoryDependency.getDir()));
        if (invalid(rootDir)) {
            throw DependencyResolutionException.directoryIsInvalid(rootDir);
        }
        return LocalDirectoryDependency.fromLocal(directoryDependency.getName(), rootDir);
    }

    @Override
    public void reset(ResolvedDependency dependency, File targetLocation) {
        LocalDirectoryDependency localDirectoryDependency = (LocalDirectoryDependency) dependency;
        IOUtils.copyDirectory(localDirectoryDependency.getRootDir(), targetLocation);
    }

    private boolean invalid(File rootDir) {
        return !rootDir.exists() || !rootDir.isDirectory();
    }
}
