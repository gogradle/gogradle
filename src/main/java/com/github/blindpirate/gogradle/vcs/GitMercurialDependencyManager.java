package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
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
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.NEWEST_COMMIT;

public abstract class GitMercurialDependencyManager extends AbstractVcsDependencyManager<GitMercurialCommit> {
    protected static final Logger LOGGER = Logging.getLogger(GitMercurialDependencyManager.class);

    private final DependencyVisitor dependencyVisitor;

    public GitMercurialDependencyManager(GlobalCacheManager cacheManager,
                                         DependencyVisitor dependencyVisitor) {
        super(cacheManager);
        this.dependencyVisitor = dependencyVisitor;
    }

    protected abstract String getDefaultBranchName();

    protected abstract GitMercurialAccessor getAccessor();

    @Override
    protected void doReset(ResolvedDependency dependency, Path globalCachePath) {
        GitMercurialResolvedDependency gitMercurialResolvedDependency = (GitMercurialResolvedDependency) dependency;
        getAccessor().checkout(globalCachePath.toFile(), gitMercurialResolvedDependency.getVersion());
    }

    @Override
    protected ResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                          File repoRoot,
                                                          GitMercurialCommit commit) {
        VcsGolangPackage pkg = (VcsGolangPackage) dependency.getPackage();

        GitMercurialResolvedDependency ret = GitMercurialResolvedDependency.builder(getVcsType())
                .withNotationDependency(dependency)
                .withName(pkg.getRootPathString())
                .withCommitId(commit.getId())
                .withTag(GitMercurialNotationDependency.class.cast(dependency).getTag())
                .withRepoUrl(getAccessor().getRemoteUrl(repoRoot))
                .withCommitTime(commit.getCommitTime())
                .build();
        GolangDependencySet dependencies = DEFAULT_STRATEGY.produce(ret, repoRoot, dependencyVisitor, BUILD);
        ret.setDependencies(dependencies);

        setVendorUpdateTimeIfNecessary(repoRoot, dependencies);
        return ret;
    }

    protected abstract VcsType getVcsType();

    private void setVendorUpdateTimeIfNecessary(File repoRoot, GolangDependencySet dependencies) {
        dependencies.flatten().stream()
                .filter(dependency -> dependency instanceof VendorResolvedDependency)
                .map(dependency -> (VendorResolvedDependency) dependency)
                .forEach(dependency -> {
                    String relativePath = toUnixString(dependency.getRelativePathToHost());
                    dependency.setUpdateTime(getAccessor().lastCommitTimeOfPath(repoRoot, relativePath));
                });
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
        return getAccessor().headCommitOfBranch(repository, getDefaultBranchName());
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
        getAccessor().checkout(repoRoot, getDefaultBranchName());

        String url = getAccessor().getRemoteUrl(repoRoot);

        LOGGER.info("Pulling {} from {}", dependency, url);

        getAccessor().pull(repoRoot);
    }

    @Override
    protected void initRepository(NotationDependency dependency, File repoRoot) {
        tryCloneWithUrls(dependency, repoRoot);
    }

    private void tryCloneWithUrls(NotationDependency dependency, File directory) {
        List<String> urls = GitMercurialNotationDependency.class.cast(dependency).getUrls();
        Assert.isNotEmpty(urls, "Urls of " + dependency + " should not be empty!");
        for (int i = 0; i < urls.size(); ++i) {
            IOUtils.clearDirectory(directory);

            String url = urls.get(i);
            try {
                getAccessor().clone(url, directory);
                return;
            } catch (Throwable e) {
                LOGGER.quiet("Cloning with url {} failed, the cause is {}", url, e.getMessage());
                if (i == urls.size() - 1) {
                    throw DependencyResolutionException.cannotCloneRepository(dependency, e);
                }
            }
        }
    }

    @Override
    protected boolean repositoryMatch(File repoRoot, NotationDependency dependency) {
        String remoteUrl = getAccessor().getRemoteUrl(repoRoot);
        List<String> urls = GitMercurialNotationDependency.class.cast(dependency).getUrls();
        return urls.contains(remoteUrl);
    }


}
