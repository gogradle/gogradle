package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.GolangRepositoryHandler;
import com.github.blindpirate.gogradle.core.GolangPackage;
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
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.git.GolangRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.NEWEST_COMMIT;

public abstract class GitMercurialDependencyManager extends AbstractVcsDependencyManager<GitMercurialCommit> {
    protected static final Logger LOGGER = Logging.getLogger(GitMercurialDependencyManager.class);

    private final DependencyVisitor dependencyVisitor;

    private final GolangRepositoryHandler repositoryHandler;

    public GitMercurialDependencyManager(GlobalCacheManager cacheManager,
                                         DependencyVisitor dependencyVisitor,
                                         GolangRepositoryHandler repositoryHandler) {
        super(cacheManager);
        this.dependencyVisitor = dependencyVisitor;
        this.repositoryHandler = repositoryHandler;
    }

    protected abstract String getDefaultBranchName();

    protected abstract GitMercurialAccessor getAccessor();

    @Override
    protected void doReset(ResolvedDependency dependency, Path globalCachePath) {
        GitMercurialResolvedDependency gitMercurialResolvedDependency = (GitMercurialResolvedDependency) dependency;
        getAccessor().checkout(globalCachePath.toFile(), gitMercurialResolvedDependency.getVersion());
    }

    @Override
    protected ResolvedDependency createResolvedDependency(NotationDependency dependency, File repoRoot, GitMercurialCommit commit) {
        VcsGolangPackage pkg = (VcsGolangPackage) dependency.getPackage();

        GitMercurialResolvedDependency ret = GitMercurialResolvedDependency.gitBuilder()
                .withNotationDependency(dependency)
                .withName(StringUtils.toUnixString(pkg.getRootPath()))
                .withCommitId(commit.getId())
                .withTag(GitMercurialNotationDependency.class.cast(dependency).getTag())
                .withRepoUrl(getAccessor().getRemoteUrl(repoRoot))
                .withCommitTime(toMilliseconds(commit.getCommitTime()))
                .build();
        GolangDependencySet dependencies = dependency.getStrategy().produce(ret, repoRoot, dependencyVisitor, BUILD);
        ret.setDependencies(dependencies);

        setVendorUpdateTimeIfNecessary(repoRoot, dependencies);
        return ret;
    }

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
            Optional<GitMercurialCommit> commit = getAccessor().findCommitByTag(repository, notationDependency.getTag());
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

        satisfiedTags.sort((tag1, tag2) -> {
            if (tag1.getSemVersion() == null) {
                return 1;
            } else if (tag2.getSemVersion() == null) {
                return -1;
            } else {
                return tag1.getSemVersion().compareTo(tag2.getSemVersion());
            }
        });

        return Optional.of(satisfiedTags.get(0));
    }


    private boolean isConcreteCommit(String commit) {
        return commit != null && !NEWEST_COMMIT.equals(commit);
    }

    @Override
    protected void updateRepository(NotationDependency dependency, File repoRoot) {
        getAccessor().checkout(repoRoot, getDefaultBranchName());

        String url = getAccessor().getRemoteUrl(repoRoot);

        GolangRepository matchedRepo = repositoryHandler.findMatchedRepository(dependency.getName());

        LOGGER.info("Pulling {} from {}", dependency, url);

        getAccessor().hardResetAndPull(repoRoot, matchedRepo.getProxyEnv());
    }

    @Override
    protected void initRepository(NotationDependency dependency, File repoRoot) {
        List<String> urls = GitMercurialNotationDependency.class.cast(dependency).getUrls();
        tryCloneWithUrls(dependency, urls, repoRoot);
    }

    private void tryCloneWithUrls(NotationDependency dependency, List<String> urls, File directory) {
        Assert.isNotEmpty(urls, "Urls of " + dependency + " should not be empty!");
        for (int i = 0; i < urls.size(); ++i) {
            IOUtils.clearDirectory(directory);

            GolangRepository matchedRepo =
                    repositoryHandler.findMatchedRepository(dependency.getName());

            String url = matchedRepo.substitute(urls.get(i));
            try {
                getAccessor().clone(url, directory, matchedRepo.getProxyEnv());
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

        for (String url : urls) {
            GolangRepository matchedRepo = repositoryHandler.findMatchedRepository(dependency.getName());
            if (remoteUrl.equals(matchedRepo.substitute(url))) {
                return true;
            }
        }
        return false;
    }


}
