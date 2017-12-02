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
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
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
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

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
            globalCacheManager.startSession((VcsGolangPackage) dependency.getPackage());

            resolveRepository(dependency);

            File vcsRoot = globalCacheManager.getGlobalCacheRepoDir();

            VERSION version = determineVersion(vcsRoot, dependency);

            resetToSpecificVersion(vcsRoot, version);

            return createResolvedDependency(dependency, vcsRoot, version, context);
        } catch (Exception e) {
            throw DependencyResolutionException.cannotResolveDependency(dependency, e);
        } finally {
            globalCacheManager.endSession();
        }
    }

    @Override
    public void install(ResolvedDependency dependency, File targetDirectory) {
        try {
            ResolvedDependency realDependency = determineDependency(dependency);
            globalCacheManager.startSession((VcsGolangPackage) determineDependency(dependency).getPackage());
            resolveRepository(realDependency);
            doInstall(dependency, realDependency, targetDirectory);
        } catch (Exception e) {
            throw DependencyInstallationException.cannotResetResolvedDependency(dependency, e);
        } finally {
            globalCacheManager.endSession();
        }
    }

    private void doInstall(ResolvedDependency dependency, ResolvedDependency realDependency, File targetDirectory) {
        File globalCachePath = globalCacheManager.getGlobalCacheRepoDir();
        doReset(realDependency, globalCachePath);

        Path srcPath = globalCachePath.toPath().resolve(determineRelativePath(dependency)).normalize();
        IOUtils.copyDependencies(srcPath.toFile(), targetDirectory, dependency.getSubpackages());
    }

    protected abstract void doReset(ResolvedDependency dependency, File globalCachePath);


    protected abstract ResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                                   File repoRoot,
                                                                   VERSION version,
                                                                   ResolveContext context);

    protected abstract void resetToSpecificVersion(File repository, VERSION version);

    protected abstract VERSION determineVersion(File repository, NotationDependency dependency);

    private void resolveRepository(GolangDependency dependency) {
        File repoRoot = globalCacheManager.getGlobalCacheRepoDir();
        List<String> expectedUrls = VcsGolangPackage.class.cast(dependency.getPackage()).getUrls();
        if (repositoryNeedInit(repoRoot)) {
            initRepository(dependency.getName(), expectedUrls, repoRoot);
            globalCacheManager.repoUpdated();
        } else if (repositoryNeedUpdate(repoRoot, dependency)) {
            updateRepository(dependency, repoRoot);
            globalCacheManager.repoUpdated();
        }
    }

    private boolean repositoryNeedUpdate(File repoRoot, GolangDependency dependency) {
        if (GogradleGlobal.isOffline()) {
            LOGGER.info("Cannot update {} in {} since it is offline now.", dependency, repoRoot);
            return false;
        } else if (globalCacheManager.currentRepositoryIsUpToDate()) {
            LOGGER.info("Skip updating {} in {} since it is up-to-date.", dependency, repoRoot);
            return false;
        } else if (!versionExistsInRepo(repoRoot, dependency)) {
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

    protected abstract boolean versionExistsInRepo(File repoRoot, GolangDependency dependency);

    protected abstract void updateRepository(GolangDependency dependency,
                                             File repoRoot);

    protected abstract void initRepository(String name, List<String> urls, File repoRoot);

    private boolean repositoryNeedInit(File repoRoot) {
        return IOUtils.dirIsEmpty(repoRoot);
    }

}
