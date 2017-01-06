package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyInstallationException;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public abstract class AbstractVcsResolver<REPOSITORY, VERSION> implements DependencyResolver {

    @Inject
    private GlobalCacheManager globalCacheManager;

    @Override
    public ResolvedDependency resolve(final NotationDependency dependency) {
        try {
            return globalCacheManager.runWithGlobalCacheLock(dependency, () -> {
                Path path = globalCacheManager.getGlobalCachePath(dependency.getName());
                return doResolve(dependency, path.toFile());
            });
        } catch (Exception e) {
            throw DependencyResolutionException.cannotResolveDependency(dependency, e);
        }
    }

    @Override
    public void reset(ResolvedDependency dependency, File targetDirectory) {
        try {
            globalCacheManager.runWithGlobalCacheLock(dependency, () -> {
                Path globalCachePath = globalCacheManager.getGlobalCachePath(dependency.getName());
                doReset(dependency, globalCachePath);
                IOUtils.copyDirectory(globalCachePath.toFile(), targetDirectory);
                return null;
            });
        } catch (Exception e) {
            throw DependencyInstallationException.cannotResetResolvedDependency(dependency, e);
        }
    }

    protected abstract void doReset(ResolvedDependency dependency, Path globalCachePath);

    private ResolvedDependency doResolve(NotationDependency dependency, File targetDirectory) {
        REPOSITORY repository = resolveToGlobalCache(dependency, targetDirectory);
        VERSION version = determineVersion(repository, dependency);
        resetToSpecificVersion(repository, version);
        return createResolvedDependency(dependency, targetDirectory, repository, version);
    }

    protected abstract ResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                                   File directory,
                                                                   REPOSITORY repository,
                                                                   VERSION version);

    protected abstract void resetToSpecificVersion(REPOSITORY repository, VERSION version);

    protected abstract VERSION determineVersion(REPOSITORY repository, NotationDependency dependency);

    private REPOSITORY resolveToGlobalCache(NotationDependency dependency, File targetDirectory) {
        Optional<REPOSITORY> repositoryInGlobalCache = ensureGlobalCacheEmptyOrMatch(dependency, targetDirectory);
        if (!repositoryInGlobalCache.isPresent()) {
            return initRepository(dependency, targetDirectory);
        } else {
            return updateRepository(repositoryInGlobalCache.get(), targetDirectory);
        }
    }

    protected abstract REPOSITORY updateRepository(REPOSITORY repository, File directory);

    protected abstract REPOSITORY initRepository(NotationDependency dependency, File directory);


    private Optional<REPOSITORY> ensureGlobalCacheEmptyOrMatch(NotationDependency dependency, File directory) {
        if (IOUtils.dirIsEmpty(directory)) {
            return Optional.empty();
        } else {
            Optional<REPOSITORY> ret = repositoryMatch(directory, dependency);
            if (ret.isPresent()) {
                return ret;
            } else {
                throw new IllegalStateException("Existing cache directory "
                        + directory.getAbsolutePath()
                        + " does not match the dependency "
                        + dependency.getName());
            }
        }
    }

    /**
     * Checks if a non-empty directory matches the dependency.
     *
     * @param directory  the directory
     * @param dependency the dependency
     * @return
     */
    protected abstract Optional<REPOSITORY> repositoryMatch(File directory, NotationDependency dependency);
}
