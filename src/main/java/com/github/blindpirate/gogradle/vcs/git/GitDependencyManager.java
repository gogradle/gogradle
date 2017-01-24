package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.resolve.AbstractVcsDependencyManager;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
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

import static com.github.blindpirate.gogradle.util.Cast.cast;
import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.NEWEST_COMMIT;

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
        GitResolvedDependency gitResolvedDependency = (GitResolvedDependency) dependency;
        Repository repository = gitAccessor.getRepository(globalCachePath.toFile());

        resetToSpecificCommit(repository, gitResolvedDependency.getVersion());
    }

    @Override
    protected GitResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                             File directory,
                                                             Repository repository,
                                                             RevCommit commit) {

        GitResolvedDependency ret = GitResolvedDependency.builder()
                .withNotationDependency(dependency)
                .withName(dependency.getPackage().getRootPath())
                .withCommitId(commit.getName())
                .withTag(cast(GitNotationDependency.class, dependency).getTag())
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
                    String relativePath = dependency.getRelativePathToHost().toString();
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
        GitNotationDependency gitNotationDependency = (GitNotationDependency) dependency;
        if (gitNotationDependency.getTag() != null) {
            Optional<RevCommit> commit = gitAccessor.findCommitByTag(repository, gitNotationDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }

            commit = gitAccessor.findCommitBySemVersion(repository, gitNotationDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }
        }

        if (isConcreteCommit(gitNotationDependency.getCommit())) {
            Optional<RevCommit> commit = gitAccessor.findCommit(repository, gitNotationDependency.getCommit());
            if (commit.isPresent()) {
                return commit.get();
            } else {
                throw DependencyResolutionException.cannotFindGitCommit(gitNotationDependency);
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

        if (GogradleGlobal.isOffline()) {
            LOGGER.info("Cannot pull {} since it is offline now.", gitAccessor.getRemoteUrl(repository));
        } else {
            gitAccessor.hardResetAndPull(dependency.getPackage().getRootPath(), repository);
        }
        return repository;
    }

    @Override
    protected Repository initRepository(NotationDependency dependency, File directory) {
        List<String> urls = GitNotationDependency.class.cast(dependency).getUrls();
        tryCloneWithUrls(dependency, urls, directory);
        return gitAccessor.getRepository(directory);
    }

    private void tryCloneWithUrls(NotationDependency dependency, List<String> urls, File directory) {
        for (int i = 0; i < urls.size(); ++i) {
            String url = urls.get(i);
            try {
                LOGGER.quiet("Cloning {} to {}", url, directory.getAbsolutePath());
                gitAccessor.cloneWithUrl(dependency.getPackage().getRootPath(), url, directory);
                return;
            } catch (Throwable e) {
                LOGGER.warn("Clone {} with url {} failed", dependency.getName(), url, e);
                if (i == urls.size() - 1) {
                    throw DependencyResolutionException.cannotCloneRepository(dependency, e);
                }
            }
        }
    }

    @Override
    protected Optional<Repository> repositoryMatch(File repoRootDir, NotationDependency dependency) {
        Repository repository = gitAccessor.getRepository(repoRootDir);
        List<String> urls = GitNotationDependency.class.cast(dependency).getUrls();

        if (urls.contains(gitAccessor.getRemoteUrl(repository))) {
            return Optional.of(repository);
        } else {
            return Optional.empty();
        }
    }
}
