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
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

public abstract class AbstractVcsDependencyManager<VERSION>
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
        resolveRepository(dependency, vcsRoot);
        VERSION version = determineVersion(vcsRoot, dependency);
        resetToSpecificVersion(vcsRoot, version);
        return createResolvedDependency(dependency, vcsRoot, version);
    }

    @Override
    public void install(ResolvedDependency dependency, File targetDirectory) {
        try {
            ResolvedDependency realDependency = determineResolvedDependency(dependency);
            globalCacheManager.runWithGlobalCacheLock(realDependency, () -> {
                restoreRepository(realDependency, targetDirectory);
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
                                                                   File repoRoot,
                                                                   VERSION version);

    protected abstract void resetToSpecificVersion(File repository, VERSION version);

    protected abstract VERSION determineVersion(File repository, NotationDependency dependency);

    private void restoreRepository(ResolvedDependency dependency, File repoRoot) {
        String url = VcsResolvedDependency.class.cast(dependency).getUrl();
        boolean repositoryNeedInit = globalCacheRepositoryNeedInit(repoRoot, Arrays.asList(url));
        if (repositoryNeedInit) {
            initRepository(dependency.getName(), Arrays.asList(url), repoRoot);
            globalCacheManager.updateCurrentDependencyLock(dependency);
        }
    }

    private void resolveRepository(NotationDependency dependency, File repoRoot) {
        List<String> expectedUrls = GitMercurialNotationDependency.class.cast(dependency).getUrls();
        boolean repositoryNeedInit = globalCacheRepositoryNeedInit(repoRoot, expectedUrls);
        if (repositoryNeedInit) {
            initRepository(dependency.getName(), expectedUrls, repoRoot);
            globalCacheManager.updateCurrentDependencyLock(dependency);
        } else if (globalCacheManager.currentDependencyIsOutOfDate(dependency)) {
            if (GogradleGlobal.isOffline()) {
                LOGGER.info("Cannot pull update {} since it is offline now.", dependency);
            } else {
                updateRepository(dependency, repoRoot);
                globalCacheManager.updateCurrentDependencyLock(dependency);
            }
        } else {
            LOGGER.info("Skipped updating {} since it is up-to-date.", dependency);
        }
    }

    protected abstract void updateRepository(NotationDependency dependency,
                                             File repoRoot);

    protected abstract void initRepository(String name, List<String> urls, File repoRoot);


    private boolean globalCacheRepositoryNeedInit(File globalCacheRepoRoot,
                                                  List<String> expectedUrls) {
        if (IOUtils.dirIsEmpty(globalCacheRepoRoot)) {
            return true;
        } else {
            String url = getCurrentRepositoryRemoteUrl(globalCacheRepoRoot);
            if (expectedUrls.contains(url)) {
                return false;
            } else {
                LOGGER.warn("Repo " + globalCacheRepoRoot.getAbsolutePath()
                        + "doesn't match url declared in dependency, it will be cleared.");
                return true;
            }
        }
    }

    protected abstract String getCurrentRepositoryRemoteUrl(File globalCacheRepoRoot);
}
