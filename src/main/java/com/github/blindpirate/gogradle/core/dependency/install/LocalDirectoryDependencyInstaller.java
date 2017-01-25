package com.github.blindpirate.gogradle.core.dependency.install;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.pack.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class LocalDirectoryDependencyInstaller implements DependencyInstaller {
    @Override
    public void install(ResolvedDependency dependency, File targetDirectory) {
        LocalDirectoryDependency realDependency = (LocalDirectoryDependency) determineDependency(dependency);
        Path realPath = realDependency.getRootDir().toPath().resolve(determineRelativePath(dependency));

        IOUtils.copyDirectory(realPath.toFile(), targetDirectory, DependencyInstallFileFilter.INSTANCE);
    }

    // TODO duplicated code
    private ResolvedDependency determineDependency(ResolvedDependency dependency) {
        if (dependency instanceof VendorResolvedDependency) {
            return VendorResolvedDependency.class.cast(dependency).getHostDependency();
        } else {
            return dependency;
        }
    }

    private Path determineRelativePath(ResolvedDependency dependency) {
        if (dependency instanceof VendorResolvedDependency) {
            return VendorResolvedDependency.class.cast(dependency).getRelativePathToHost();
        } else {
            return Paths.get(".");
        }
    }
}
