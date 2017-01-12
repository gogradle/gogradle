package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;
import com.github.blindpirate.gogradle.core.exceptions.DependencyInstallationException;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.Cast;
import com.github.blindpirate.gogradle.util.IOUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class AbstractVcsDependencyManager<REPOSITORY, VERSION>
        implements DependencyResolver, DependencyInstaller {

    private final GlobalCacheManager globalCacheManager;

    public AbstractVcsDependencyManager(GlobalCacheManager cacheManager) {
        this.globalCacheManager = cacheManager;
    }

    @Override
    public ResolvedDependency resolve(final NotationDependency dependency) {
        try {

            return globalCacheManager.runWithGlobalCacheLock(dependency, () -> {
                File vcsRoot = globalCacheManager.getGlobalCachePath(dependency.getName()).toFile();
                ResolvedDependency vcsResolvedDependency = resolveVcs(dependency, vcsRoot);
                return extractVendorDependencyIfNecessary(dependency, vcsResolvedDependency);
            });
        } catch (Exception e) {
            throw DependencyResolutionException.cannotResolveDependency(dependency, e);
        }
    }

    private ResolvedDependency extractVendorDependencyIfNecessary(NotationDependency dependency,
                                                                  ResolvedDependency resolvedDependency) {
        if (dependency instanceof VendorNotationDependency) {
            VendorNotationDependency vendorNotationDependency = (VendorNotationDependency) dependency;
            Optional<VendorResolvedDependency> result = resolvedDependency.getDependencies().flatten()
                    .stream()
                    .filter(d -> d instanceof VendorResolvedDependency)
                    .map(d -> (VendorResolvedDependency) d)
                    .filter(d -> d.getRelativePathToHost().toString().equals(vendorNotationDependency.getVendorPath()))
                    .findFirst();
            if (result.isPresent()) {
                return result.get();
            } else {
                throw DependencyResolutionException.vendorNotExist(vendorNotationDependency, resolvedDependency);
            }
        } else {
            return resolvedDependency;
        }
    }

    private ResolvedDependency resolveVcs(NotationDependency dependency, File vcsRoot) {
        REPOSITORY repository = resolveToGlobalCache(dependency, vcsRoot);
        VERSION version = determineVersion(repository, dependency);
        resetToSpecificVersion(repository, version);
        return createResolvedDependency(dependency, vcsRoot, repository, version);
    }

    @Override
    public void install(ResolvedDependency dependency, File targetDirectory) {
        try {
            globalCacheManager.runWithGlobalCacheLock(dependency, () -> {
                installUnderLock(dependency, targetDirectory);
                return null;
            });
        } catch (Exception e) {
            throw DependencyInstallationException.cannotResetResolvedDependency(dependency, e);
        }
    }


    private void installUnderLock(ResolvedDependency dependency, File targetDirectory) {
        ResolvedDependency realDependency = determineResolvedDependency(dependency);
        Path globalCachePath = globalCacheManager.getGlobalCachePath(realDependency.getName());
        doReset(realDependency, globalCachePath);

        Path srcPath = globalCachePath.resolve(determineRelativePath(dependency));
        IOUtils.copyDirectory(srcPath.toFile(), targetDirectory);
    }

    private ResolvedDependency determineResolvedDependency(ResolvedDependency dependency) {
        if (dependency instanceof VendorResolvedDependency) {
            return Cast.cast(VendorResolvedDependency.class, dependency).getHostDependency();
        } else {
            return dependency;
        }
    }

    private Path determineRelativePath(ResolvedDependency dependency) {
        if (dependency instanceof VendorResolvedDependency) {
            return Cast.cast(VendorResolvedDependency.class, dependency).getRelativePathToHost();
        } else {
            return Paths.get(".");
        }
    }


    protected abstract void doReset(ResolvedDependency dependency, Path globalCachePath);


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
