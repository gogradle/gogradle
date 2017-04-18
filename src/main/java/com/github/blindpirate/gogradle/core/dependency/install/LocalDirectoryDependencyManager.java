package com.github.blindpirate.gogradle.core.dependency.install;

import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.CacheEnabledDependencyResolverMixin;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;

@Singleton
public class LocalDirectoryDependencyManager
        implements VendorSupportMixin, DependencyManager, CacheEnabledDependencyResolverMixin {
    private final ProjectCacheManager projectCacheManager;

    @Inject
    public LocalDirectoryDependencyManager(ProjectCacheManager projectCacheManager) {
        this.projectCacheManager = projectCacheManager;
    }

    @Override
    public void install(ResolvedDependency dependency, File targetDirectory) {
        LocalDirectoryDependency realDependency = (LocalDirectoryDependency) determineDependency(dependency);
        Path realPath = realDependency.getRootDir().toPath().resolve(determineRelativePath(dependency));

        IOUtils.copyDirectory(realPath.toFile(), targetDirectory, DependencyInstallFileFilter.INSTANCE);
    }

    @Override
    public ProjectCacheManager getProjectCacheManager() {
        return projectCacheManager;
    }

    @Override
    public ResolvedDependency doResolve(ResolveContext context, NotationDependency dependency) {
        LocalDirectoryDependency ret = (LocalDirectoryDependency) dependency;
        ret.setDependencies(context.produceTransitiveDependencies(ret, ret.getRootDir()));
        return ret;
    }
}
