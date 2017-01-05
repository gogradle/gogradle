package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.IOUtils;

import javax.inject.Inject;
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
                return doResolve(dependency, path);
            });
        } catch (Exception e) {
            throw DependencyResolutionException.cannotResolveDependency(dependency, e);
        }
    }

    private ResolvedDependency doResolve(NotationDependency dependency, Path path) {
        REPOSITORY repository = resolveToGlobalCache(dependency, path);
        VERSION version = determineVersion(repository, dependency);
        resetToSpecificVersion(repository, version);
        return createResolvedDependency(dependency, path, repository, version);
    }

    protected abstract ResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                                   Path path,
                                                                   REPOSITORY repository,
                                                                   VERSION version);

    protected abstract void resetToSpecificVersion(REPOSITORY repository, VERSION version);

    protected abstract VERSION determineVersion(REPOSITORY repository, NotationDependency dependency);

    private REPOSITORY resolveToGlobalCache(NotationDependency dependency, Path path) {
        Optional<REPOSITORY> repositoryInGlobalCache = ensureGlobalCacheEmptyOrMatch(dependency, path);
        if (!repositoryInGlobalCache.isPresent()) {
            return initRepository(dependency, path);
        } else {
            return updateRepository(repositoryInGlobalCache.get(), path);
        }
    }

    protected abstract REPOSITORY updateRepository(REPOSITORY repository, Path path);

    protected abstract REPOSITORY initRepository(NotationDependency dependency, Path path);


    private Optional<REPOSITORY> ensureGlobalCacheEmptyOrMatch(NotationDependency dependency, Path path) {
        if (IOUtils.dirIsEmpty(path)) {
            return Optional.empty();
        } else {
            Optional<REPOSITORY> ret = repositoryMatch(path, dependency);
            if (ret.isPresent()) {
                return ret;
            } else {
                throw new IllegalStateException("Existing cache directory "
                        + path.toAbsolutePath().toString()
                        + " does not match the dependency "
                        + dependency.getName());
            }
        }
    }

    /**
     * Checks if a non-empty directory matches the dependency.
     *
     * @param repoPath   the directory
     * @param dependency the dependency
     * @return
     */
    protected abstract Optional<REPOSITORY> repositoryMatch(Path repoPath, NotationDependency dependency);
}
