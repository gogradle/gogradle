package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
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

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.dependency.produce.strategy.DependencyProduceStrategy.DEFAULT_STRATEGY;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.NEWEST_COMMIT;

public abstract class GitMercurialDependencyManager extends AbstractVcsDependencyManager<GitMercurialCommit> {
    protected static final Logger LOGGER = Logging.getLogger(GitMercurialDependencyManager.class);

    private final DependencyVisitor dependencyVisitor;

    public GitMercurialDependencyManager(GlobalCacheManager cacheManager,
                                         DependencyVisitor dependencyVisitor) {
        super(cacheManager);
        this.dependencyVisitor = dependencyVisitor;
    }

    protected abstract GitMercurialAccessor getAccessor();

    @Override
    protected void doReset(ResolvedDependency dependency, Path globalCachePath) {
        VcsResolvedDependency vcsResolvedDependency = (VcsResolvedDependency) dependency;
        getAccessor().checkout(globalCachePath.toFile(), vcsResolvedDependency.getVersion());
    }

    @Override
    protected ResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                          File repoRoot,
                                                          GitMercurialCommit commit) {
        VcsGolangPackage pkg = (VcsGolangPackage) dependency.getPackage();

        VcsResolvedDependency ret = VcsResolvedDependency.builder(getVcsType())
                .withNotationDependency(dependency)
                .withName(pkg.getRootPathString())
                .withCommitId(commit.getId())
                .withTag(GitMercurialNotationDependency.class.cast(dependency).getTag())
                .withUrl(getAccessor().getRemoteUrl(repoRoot))
                .withCommitTime(commit.getCommitTime())
                .build();
        GolangDependencySet dependencies = DEFAULT_STRATEGY.produce(ret, repoRoot, dependencyVisitor, BUILD);
        ret.setDependencies(dependencies);

        return ret;
    }

    protected abstract VcsType getVcsType();

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

        if (isConcreteCommit(notationDependency.getCommit())) {
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


    private boolean isConcreteCommit(String commit) {
        return commit != null && !NEWEST_COMMIT.equals(commit);
    }

    @Override
    protected void updateRepository(NotationDependency dependency, File repoRoot) {
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
                LOGGER.quiet("Cloning with url {} failed, the cause is {}", url, e.getMessage());
                if (i == urls.size() - 1) {
                    throw DependencyResolutionException.cannotCloneRepository(name, e);
                }
            }
        }
    }

}
