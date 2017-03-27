package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstallFileFilter;
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;
import com.github.blindpirate.gogradle.core.exceptions.DependencyInstallationException;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

public abstract class AbstractVcsDependencyManager<REPOSITORY, VERSION>
        implements DependencyResolver, DependencyInstaller {

    private static final Logger LOGGER = Logging.getLogger(AbstractVcsDependencyManager.class);

    private final GlobalCacheManager globalCacheManager;

    public AbstractVcsDependencyManager(GlobalCacheManager cacheManager) {
        this.globalCacheManager = cacheManager;
    }

    @Override
    public ResolvedDependency resolve(GolangConfiguration configuration, NotationDependency dependency) {
        Optional<ResolvedDependency> resultInCache = configuration.getDependencyRegistry().getFromCache(dependency);
        if (resultInCache.isPresent()) {
            return resultInCache.get();
        }
        ResolvedDependency ret = doResolve(dependency);

        configuration.getDependencyRegistry().putIntoCache(dependency, ret);
        return ret;
    }

    private ResolvedDependency doResolve(NotationDependency dependency) {
        LOGGER.quiet("Resolving {}", dependency);
        try {
            NotationDependency vcsNotationDependency = extractVcsHostDependency(dependency);

            return globalCacheManager.runWithGlobalCacheLock(vcsNotationDependency, () -> {
                File vcsRoot = globalCacheManager.getGlobalPackageCachePath(vcsNotationDependency.getName()).toFile();
                ResolvedDependency vcsResolvedDependency = resolveVcs(vcsNotationDependency, vcsRoot);
                return extractVendorDependencyIfNecessary(dependency, vcsResolvedDependency);
            });
        } catch (Exception e) {
            throw DependencyResolutionException.cannotResolveDependency(dependency, e);
        }
    }

    private NotationDependency extractVcsHostDependency(NotationDependency dependency) {
        if (dependency instanceof VendorNotationDependency) {
            return VendorNotationDependency.class.cast(dependency).getHostNotationDependency();
        } else {
            return dependency;
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
                    .filter(d ->
                            toUnixString(d.getRelativePathToHost()).equals(vendorNotationDependency.getVendorPath()))
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
            ResolvedDependency realDependency = determineResolvedDependency(dependency);
            globalCacheManager.runWithGlobalCacheLock(realDependency, () -> {
                installUnderLock(dependency, targetDirectory);
                return null;
            });
        } catch (Exception e) {
            throw DependencyInstallationException.cannotResetResolvedDependency(dependency, e);
        }
    }

    private void installUnderLock(ResolvedDependency dependency, File targetDirectory) {
        ResolvedDependency realDependency = determineResolvedDependency(dependency);
        Path globalCachePath = globalCacheManager.getGlobalPackageCachePath(realDependency.getName());
        doReset(realDependency, globalCachePath);

        Path srcPath = globalCachePath.resolve(determineRelativePath(dependency));
        IOUtils.copyDirectory(srcPath.toFile(), targetDirectory, DependencyInstallFileFilter.INSTANCE);
    }

    private ResolvedDependency determineResolvedDependency(ResolvedDependency dependency) {
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
            REPOSITORY ret = initRepository(dependency, targetDirectory);
            globalCacheManager.updateCurrentDependencyLock();
            return ret;
        } else if (globalCacheManager.currentDependencyIsOutOfDate()) {
            if (GogradleGlobal.isOffline()) {
                LOGGER.info("Cannot pull update {} since it is offline now.", dependency);
                return repositoryInGlobalCache.get();
            } else {
                updateRepository(dependency, repositoryInGlobalCache.get(), targetDirectory);
                globalCacheManager.updateCurrentDependencyLock();
                return repositoryInGlobalCache.get();
            }
        } else {
            LOGGER.info("Skipped updating {} since it is up-to-date.", dependency);
            return repositoryInGlobalCache.get();
        }
    }

    protected abstract REPOSITORY updateRepository(NotationDependency dependency,
                                                   REPOSITORY repository,
                                                   File directory);

    protected abstract REPOSITORY initRepository(NotationDependency dependency, File directory);


    private Optional<REPOSITORY> ensureGlobalCacheEmptyOrMatch(NotationDependency dependency, File directory) {
        if (IOUtils.dirIsEmpty(directory)) {
            return Optional.empty();
        } else {
            Optional<REPOSITORY> ret = repositoryMatch(directory, dependency);
            if (ret.isPresent()) {
                return ret;
            } else {
                LOGGER.warn("Repo " + directory.getAbsolutePath()
                        + "doesn't match url declared in dependency, it will be cleared.");
                return Optional.empty();
            }
        }
    }

    /**
     * Checks if a non-empty directory matches the dependency.
     *
     * @param directory  the directory
     * @param dependency the dependency
     * @return {@code Optional.of()} if matched, {@code Optional.empty()} otherwise.
     */
    protected abstract Optional<REPOSITORY> repositoryMatch(File directory, NotationDependency dependency);
}
