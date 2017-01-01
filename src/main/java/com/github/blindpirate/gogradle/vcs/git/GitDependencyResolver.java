package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.AbstractVcsResolver;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.Cast;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.NEWEST_COMMIT;

@Singleton
public class GitDependencyResolver extends AbstractVcsResolver<Repository, RevCommit> {

    public static final String DEFAULT_BRANCH = "master";
    private static final Logger LOGGER = Logging.getLogger(GitDependencyResolver.class);

    @Inject
    private GitAccessor gitAccessor;

    @Override
    protected AbstractResolvedDependency createResolvedDependency(NotationDependency dependency,
                                                                  Path path,
                                                                  Repository repository,
                                                                  RevCommit commit) {

        return GitResolvedDependency.builder()
                .withName(dependency.getName())
                .withCommitId(commit.getName())
                .withRepoUrl(gitAccessor.getRemoteUrl(repository))
                .withCommitTime(toMilliseconds(commit.getCommitTime()))
                .build();
    }

    @Override
    protected void resetToSpecificVersion(Repository repository, RevCommit commit) {
        gitAccessor.resetToCommit(repository, commit.getName());
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
        // TODO the default branch may not be master
        return gitAccessor.headCommitOfBranch(repository, DEFAULT_BRANCH).get();
    }

    private boolean isConcreteCommit(String commit) {
        return commit != null && !NEWEST_COMMIT.equals(commit);
    }

    @Override
    protected Repository updateRepository(Repository repository, Path path) {
        return gitAccessor.hardResetAndUpdate(repository);
    }

    @Override
    protected Repository initRepository(NotationDependency dependency, Path path) {
        List<String> urls = determineUrls(dependency);
        tryCloneWithUrls(dependency, urls, path);
        return gitAccessor.getRepository(path);
    }

    private List<String> determineUrls(NotationDependency dependency) {
        String urlSpecifiedByUser = Cast.cast(GitNotationDependency.class, dependency).getUrl();
        if (urlSpecifiedByUser != null) {
            return Arrays.asList(urlSpecifiedByUser);
        } else {
            return Cast.cast(GitNotationDependency.class, dependency).getUrls();
        }
    }

    private void tryCloneWithUrls(NotationDependency dependency, List<String> urls, Path path) {
        for (int i = 0; i < urls.size(); ++i) {
            String url = urls.get(i);
            try {
                gitAccessor.cloneWithUrl(url, path);
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
    protected Optional<Repository> repositoryMatch(Path repoPath, NotationDependency dependency) {
        Repository repository = gitAccessor.getRepository(repoPath);
        List<String> urls = determineUrls(dependency);
        Set<String> remoteUrls = gitAccessor.getRemoteUrls(repository);

        if (Collections.disjoint(urls, remoteUrls)) {
            return Optional.empty();
        } else {
            return Optional.of(repository);
        }
    }
}
