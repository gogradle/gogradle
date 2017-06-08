/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.install.VendorSupportMixin;
import com.github.blindpirate.gogradle.core.exceptions.DependencyInstallationException;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.github.blindpirate.gogradle.core.cache.CacheScope.PERSISTENCE;
import static java.util.Arrays.asList;

public abstract class AbstractVcsDependencyManager<VERSION>
        implements CacheEnabledDependencyResolverMixin, VendorSupportMixin, DependencyManager {

    private static final Logger LOGGER = Logging.getLogger(AbstractVcsDependencyManager.class);

    private final GlobalCacheManager globalCacheManager;
    private final ProjectCacheManager projectCacheManager;

    public AbstractVcsDependencyManager(GlobalCacheManager globalCacheManager,
                                        ProjectCacheManager projectCacheManager) {
        this.globalCacheManager = globalCacheManager;
        this.projectCacheManager = projectCacheManager;
    }

    @Override
    public ProjectCacheManager getProjectCacheManager() {
        return projectCacheManager;
    }

    @Override
    public ResolvedDependency doResolve(ResolveContext context, NotationDependency dependency) {
        try {
            return globalCacheManager.runWithGlobalCacheLock(dependency, () -> {
                File vcsRoot = globalCacheManager.getGlobalPackageCachePath(dependency.getName()).toFile();
                return resolveVcs(dependency, vcsRoot, context);
            });
        } catch (Exception e) {
            throw DependencyResolutionException.cannotResolveDependency(dependency, e);
        }
    }

    private ResolvedDependency resolveVcs(NotationDependency dependency, File vcsRoot, ResolveContext context) {
        resolveRepository(dependency, vcsRoot);
        VERSION version = determineVersion(vcsRoot, dependency);
        resetToSpecificVersion(vcsRoot, version);
        return createResolvedDependency(dependency, vcsRoot, version, context);
    }

    @Override
    public void install(ResolvedDependency dependency, File targetDirectory) {
        try {
            ResolvedDependency realDependency = determineDependency(dependency);
            globalCacheManager.runWithGlobalCacheLock(realDependency, () -> {
                restoreRepository(realDependency);
                installUnderLock(dependency, targetDirectory);
                return null;
            });
        } catch (Exception e) {
            throw DependencyInstallationException.cannotResetResolvedDependency(dependency, e);
        }
    }

    private void installUnderLock(ResolvedDependency dependency, File targetDirectory) {
        ResolvedDependency realDependency = determineDependency(dependency);
        Path globalCachePath = globalCacheManager.getGlobalPackageCachePath(realDependency.getName());
        doReset(realDependency, globalCachePath);

        Path srcPath = globalCachePath.resolve(determineRelativePath(dependency)).normalize();
        IOUtils.copyDependencies(srcPath.toFile(), targetDirectory, dependency.getSubpackages());
    }

    protected abstract void doReset(ResolvedDependency dependency, Path globalCachePath);


    protected abstract ResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                                   File repoRoot,
                                                                   VERSION version,
                                                                   ResolveContext context);

    protected abstract void resetToSpecificVersion(File repository, VERSION version);

    protected abstract VERSION determineVersion(File repository, NotationDependency dependency);

    private void restoreRepository(ResolvedDependency dependency) {
        File globalCacheRepoRoot = globalCacheManager.getGlobalPackageCachePath(dependency.getName()).toFile();

        String url = VcsResolvedDependency.class.cast(dependency).getUrl();
        if (repositoryNeedInit(globalCacheRepoRoot, asList(url))) {
            initRepository(dependency.getName(), asList(url), globalCacheRepoRoot);
            globalCacheManager.updateCurrentDependencyLock(dependency);
        } else if (!concreteVersionExistInRepo(globalCacheRepoRoot, dependency)) {
            updateRepository(dependency, globalCacheRepoRoot);
        }
    }

    private void resolveRepository(NotationDependency dependency, File repoRoot) {
        List<String> expectedUrls = GitMercurialNotationDependency.class.cast(dependency).getUrls();
        if (repositoryNeedInit(repoRoot, expectedUrls)) {
            initRepository(dependency.getName(), expectedUrls, repoRoot);
            globalCacheManager.updateCurrentDependencyLock(dependency);
        } else if (repositoryNeedUpdate(repoRoot, dependency)) {
            updateRepository(dependency, repoRoot);
            globalCacheManager.updateCurrentDependencyLock(dependency);
        }
    }

    protected boolean repositoryNeedUpdate(File repoRoot, NotationDependency dependency) {
        if (GogradleGlobal.isOffline()) {
            LOGGER.info("Cannot update {} in {} since it is offline now.", dependency, repoRoot);
            return false;
        } else if (globalCacheManager.currentRepositoryIsUpToDate(dependency)) {
            LOGGER.info("Skip updating {} in {} since it is up-to-date.", dependency, repoRoot);
            return false;
        } else if (dependency.getCacheScope() == PERSISTENCE && !concreteVersionExistInRepo(repoRoot, dependency)) {
            LOGGER.info("{} does not exist in {}, updating will be performed.", dependency, repoRoot);
            return true;
        } else if (GogradleGlobal.isRefreshDependencies()) {
            LOGGER.info("Updating {} in {} since refresh flag is present.", dependency, repoRoot);
            return true;
        } else {
            LOGGER.info("Skip updating {} in {}.", dependency, repoRoot);
            return false;
        }
    }

    protected abstract boolean concreteVersionExistInRepo(File repoRoot, GolangDependency dependency);

    protected abstract void updateRepository(GolangDependency dependency,
                                             File repoRoot);

    protected abstract void initRepository(String name, List<String> urls, File repoRoot);


    private boolean repositoryNeedInit(File globalCacheRepoRoot,
                                       List<String> expectedUrls) {
        if (IOUtils.dirIsEmpty(globalCacheRepoRoot)) {
            return true;
        } else {
            String url = getCurrentRepositoryRemoteUrl(globalCacheRepoRoot);
            if (expectedUrls.contains(url)) {
                return false;
            } else {
                LOGGER.warn("Global cache " + globalCacheRepoRoot.getAbsolutePath()
                        + " doesn't match url in dependency, it will be cleared.");
                return true;
            }
        }
    }

    protected abstract String getCurrentRepositoryRemoteUrl(File globalCacheRepoRoot);
}
