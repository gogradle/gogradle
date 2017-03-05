package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.resolve.AbstractVcsDependencyManager;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;
import com.github.blindpirate.gogradle.vcs.GitMercurialResolvedDependency;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.NEWEST_COMMIT;

@Singleton
public class GitDependencyManager extends AbstractVcsDependencyManager<Repository, RevCommit> {

    public static final String DEFAULT_BRANCH = "master";
    private static final Logger LOGGER = Logging.getLogger(GitDependencyManager.class);

    private final GitAccessor gitAccessor;

    private final DependencyVisitor visitor;

    @Inject
    public GitDependencyManager(GlobalCacheManager cacheManager,
                                GitAccessor gitAccessor,
                                DependencyVisitor visitor,
                                DependencyRegistry dependencyRegistry) {
        super(cacheManager, dependencyRegistry);
        this.gitAccessor = gitAccessor;
        this.visitor = visitor;
    }

    @Override
    protected void doReset(ResolvedDependency dependency, Path globalCachePath) {
        GitMercurialResolvedDependency gitMercurialResolvedDependency = (GitMercurialResolvedDependency) dependency;
        Repository repository = gitAccessor.getRepository(globalCachePath.toFile());

        resetToSpecificCommit(repository, gitMercurialResolvedDependency.getVersion());
    }

    @Override
    protected GitMercurialResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                                      File directory,
                                                                      Repository repository,
                                                                      RevCommit commit) {

        GitMercurialResolvedDependency ret = GitMercurialResolvedDependency.gitBuilder()
                .withNotationDependency(dependency)
                .withName(dependency.getPackage().getRootPath())
                .withCommitId(commit.getName())
                .withTag(GitMercurialNotationDependency.class.cast(dependency).getTag())
                .withRepoUrl(gitAccessor.getRemoteUrl(repository))
                .withCommitTime(toMilliseconds(commit.getCommitTime()))
                .build();
        GolangDependencySet dependencies = dependency.getStrategy().produce(ret, directory, visitor);
        ret.setDependencies(dependencies);

        setVendorUpdateTimeIfNecessary(repository, dependencies);
        return ret;
    }

    private void setVendorUpdateTimeIfNecessary(Repository repository, GolangDependencySet dependencies) {
        dependencies.flatten().stream()
                .filter(dependency -> dependency instanceof VendorResolvedDependency)
                .map(dependency -> (VendorResolvedDependency) dependency)
                .forEach(dependency -> {
                    String relativePath = toUnixString(dependency.getRelativePathToHost());
                    dependency.setUpdateTime(gitAccessor.lastCommitTimeOfPath(repository, relativePath));
                });
    }

    @Override
    protected void resetToSpecificVersion(Repository repository, RevCommit commit) {
        resetToSpecificCommit(repository, commit.getName());
    }

    private void resetToSpecificCommit(Repository repository, String commitId) {
        gitAccessor.checkout(repository, commitId);
    }

    @Override
    protected RevCommit determineVersion(Repository repository, NotationDependency dependency) {
        GitMercurialNotationDependency notationDependency = (GitMercurialNotationDependency) dependency;
        if (notationDependency.getTag() != null) {
            Optional<RevCommit> commit = gitAccessor.findCommitByTag(repository, notationDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }

            commit = gitAccessor.findCommitBySemVersion(repository, notationDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }
        }

        if (isConcreteCommit(notationDependency.getCommit())) {
            Optional<RevCommit> commit = gitAccessor.findCommit(repository, notationDependency.getCommit());
            if (commit.isPresent()) {
                return commit.get();
            } else {
                throw DependencyResolutionException.cannotFindGitCommit(notationDependency);
            }
        }

        // use HEAD of master branch
        return gitAccessor.headCommitOfBranch(repository, DEFAULT_BRANCH).get();
    }

    private boolean isConcreteCommit(String commit) {
        return commit != null && !NEWEST_COMMIT.equals(commit);
    }

    @Override
    protected Repository updateRepository(NotationDependency dependency, Repository repository, File directory) {
        gitAccessor.checkout(repository, DEFAULT_BRANCH);

        LOGGER.info("Pulling {} from {}", dependency, gitAccessor.getRemoteUrl(repository));
        gitAccessor.hardResetAndPull(dependency.getPackage().getRootPath(), repository);
        return repository;
    }

    @Override
    protected Repository initRepository(NotationDependency dependency, File directory) {
        List<String> urls = GitMercurialNotationDependency.class.cast(dependency).getUrls();
        tryCloneWithUrls(dependency, urls, directory);
        return gitAccessor.getRepository(directory);
    }

    private void tryCloneWithUrls(NotationDependency dependency, List<String> urls, File directory) {
        for (int i = 0; i < urls.size(); ++i) {
            IOUtils.clearDirectory(directory);

            String url = urls.get(i);
            try {
                gitAccessor.cloneWithUrl(dependency.getPackage().getRootPath(), url, directory);
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
    protected Optional<Repository> repositoryMatch(File repoRootDir, NotationDependency dependency) {
        Repository repository = gitAccessor.getRepository(repoRootDir);
        List<String> urls = GitMercurialNotationDependency.class.cast(dependency).getUrls();

        if (urls.contains(gitAccessor.getRemoteUrl(repository))) {
            return Optional.of(repository);
        } else {
            return Optional.empty();
        }
    }
}
