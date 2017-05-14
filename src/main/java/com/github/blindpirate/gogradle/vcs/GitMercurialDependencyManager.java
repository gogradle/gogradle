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

package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.AbstractVcsDependencyManager;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.core.cache.CacheScope.PERSISTENCE;

public abstract class GitMercurialDependencyManager extends AbstractVcsDependencyManager<GitMercurialCommit> {
    protected static final Logger LOGGER = Logging.getLogger(GitMercurialDependencyManager.class);

    public GitMercurialDependencyManager(GlobalCacheManager globalCacheManager,
                                         ProjectCacheManager projectCacheManager) {
        super(globalCacheManager, projectCacheManager);
    }

    protected abstract GitMercurialAccessor getAccessor();

    @Override
    protected void doReset(ResolvedDependency dependency, Path globalCachePath) {
        File repoRoot = globalCachePath.toFile();

        VcsResolvedDependency vcsResolvedDependency = (VcsResolvedDependency) dependency;

        getAccessor().checkout(repoRoot, vcsResolvedDependency.getVersion());
    }

    @Override
    protected ResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                          File repoRoot,
                                                          GitMercurialCommit commit,
                                                          ResolveContext context) {
        VcsResolvedDependency ret = VcsResolvedDependency.builder(getVcsType())
                .withNotationDependency(dependency)
                .withCommitId(commit.getId())
                .withUrl(getAccessor().getRemoteUrl(repoRoot))
                .withCommitTime(commit.getCommitTime())
                .build();

        GolangDependencySet dependencies = context.produceTransitiveDependencies(ret, repoRoot);
        ret.setDependencies(dependencies);

        return ret;
    }

    protected abstract VcsType getVcsType();

    @Override
    protected boolean concreteVersionExistInRepo(File repoRoot, GolangDependency dependency) {
        String commit = dependency instanceof GitMercurialNotationDependency
                ? GitMercurialNotationDependency.class.cast(dependency).getCommit()
                : VcsResolvedDependency.class.cast(dependency).getVersion();
        return getAccessor().findCommit(repoRoot, commit).isPresent();
    }

    @Override
    protected void resetToSpecificVersion(File repository, GitMercurialCommit commit) {
        getAccessor().checkout(repository, commit.getId());
    }

    @Override
    protected GitMercurialCommit determineVersion(File repository, NotationDependency dependency) {
        GitMercurialNotationDependency notationDependency = (GitMercurialNotationDependency) dependency;
        if (notationDependency.getTag() != null) {
            Optional<GitMercurialCommit> commit = getAccessor()
                    .findCommitByTag(repository, notationDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }

            commit = findCommitBySemVersion(repository, notationDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            } else {
                throw DependencyResolutionException.cannotFindGitTag(dependency, notationDependency.getTag());
            }
        }

        if (notationDependency.getCacheScope() == PERSISTENCE) {
            Optional<GitMercurialCommit> commit = getAccessor().findCommit(repository, notationDependency.getCommit());
            if (commit.isPresent()) {
                return commit.get();
            } else {
                throw DependencyResolutionException.cannotFindGitCommit(notationDependency);
            }
        }

        // use HEAD of master branch
        return getAccessor().headCommitOfBranch(repository, getAccessor().getDefaultBranch(repository));
    }

    private Optional<GitMercurialCommit> findCommitBySemVersion(File repository, String semVersionExpression) {
        List<GitMercurialCommit> tags = getAccessor().getAllTags(repository);

        List<GitMercurialCommit> satisfiedTags = tags.stream()
                .filter(tag -> tag.satisfies(semVersionExpression))
                .collect(Collectors.toList());

        if (satisfiedTags.isEmpty()) {
            return Optional.empty();
        }

        satisfiedTags.sort((tag1, tag2) -> tag2.getSemVersion().compareTo(tag1.getSemVersion()));

        return Optional.of(satisfiedTags.get(0));
    }

    @Override
    protected void updateRepository(GolangDependency dependency, File repoRoot) {
        getAccessor().checkout(repoRoot, getAccessor().getDefaultBranch(repoRoot));

        String url = getAccessor().getRemoteUrl(repoRoot);

        LOGGER.info("Pulling {} from {}", dependency, url);

        getAccessor().update(repoRoot);
    }

    @Override
    protected void initRepository(String dependencyName, List<String> urls, File repoRoot) {
        tryCloneWithUrls(dependencyName, urls, repoRoot);
    }

    protected String getCurrentRepositoryRemoteUrl(File globalCacheRepoRoot) {
        return getAccessor().getRemoteUrl(globalCacheRepoRoot);
    }

    private void tryCloneWithUrls(String name, List<String> urls, File directory) {
        Assert.isNotEmpty(urls, "Urls of " + name + " should not be empty!");
        for (int i = 0; i < urls.size(); ++i) {
            IOUtils.clearDirectory(directory);

            String url = urls.get(i);
            try {
                getAccessor().clone(url, directory);
                return;
            } catch (Throwable e) {
                LOGGER.debug("Cloning with url {} failed, the cause is {}", url, e.getMessage());
                if (i == urls.size() - 1) {
                    throw DependencyResolutionException.cannotCloneRepository(name, e);
                }
            }
        }
    }

}
