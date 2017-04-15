package com.github.blindpirate.gogradle.core.dependency.install;

import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;

@Singleton
public class LocalDirectoryDependencyInstaller implements VendorSupportMixin, DependencyInstaller {
    @Override
    public void install(ResolvedDependency dependency, File targetDirectory) {
        LocalDirectoryDependency realDependency = (LocalDirectoryDependency) determineDependency(dependency);
        Path realPath = realDependency.getRootDir().toPath().resolve(determineRelativePath(dependency));

        IOUtils.copyDirectory(realPath.toFile(), targetDirectory, DependencyInstallFileFilter.INSTANCE);
    }
}
