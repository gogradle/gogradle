package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.VcsTempFileModule;
import com.github.blindpirate.gogradle.core.dependency.GitDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.core.pack.AbstractVcsResolver;
import com.github.blindpirate.gogradle.util.Cast;
import com.google.common.collect.ImmutableMap;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.blindpirate.gogradle.core.dependency.GitDependency.COMMIT_KEY;
import static com.github.blindpirate.gogradle.core.dependency.GitDependency.NEWEST_COMMIT;
import static com.github.blindpirate.gogradle.core.dependency.GitDependency.URLS_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;

@Singleton
public class GitDependencyResolver extends AbstractVcsResolver<Repository, RevCommit> {

    public static final String DEFAULT_BRANCH = "master";
    private static final Logger LOGGER = Logging.getLogger(GitDependencyResolver.class);

    @Inject
    private GitAccessor gitAccessor;

    @Override
    protected GolangPackageModule createModule(GolangDependency dependency,
                                               Path path,
                                               Repository repository,
                                               RevCommit commit) {
        GitDependency gitDependency = (GitDependency) dependency;
        Map<String, String> lockedNotation = ImmutableMap.of(
                NAME_KEY, gitDependency.getName(),
                URLS_KEY, gitAccessor.getRemoteUrl(repository),
                COMMIT_KEY, commit.getName()
        );
        return new VcsTempFileModule(dependency.getName(),
                path,
                toMilliseconds(commit.getCommitTime()),
                lockedNotation);
    }

    @Override
    protected void resetToSpecificVersion(Repository repository, RevCommit commit) {
        gitAccessor.resetToCommit(repository, commit.getName());
    }

    @Override
    protected RevCommit determineVersion(Repository repository, GolangDependency dependency) {
        GitDependency gitDependency = (GitDependency) dependency;
        if (gitDependency.getTag() != null) {
            Optional<RevCommit> commit = gitAccessor.findCommitByTag(repository, gitDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }

            commit = gitAccessor.findCommitBySemVersion(repository, gitDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }
        }

        if (isConcreteCommit(gitDependency.getCommit())) {
            Optional<RevCommit> commit = gitAccessor.findCommit(repository, gitDependency.getCommit());
            if (commit.isPresent()) {
                return commit.get();
            } else {
                throw DependencyResolutionException.cannotFindGitCommit(gitDependency);
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
    protected Repository initRepository(GolangDependency dependency, Path path) {
        List<String> urls = determineUrls(dependency);
        tryCloneWithUrls(dependency, urls, path);
        return gitAccessor.getRepository(path);
    }

    private List<String> determineUrls(GolangDependency dependency) {
        String urlSpecifiedByUser = Cast.cast(GitDependency.class, dependency).getUrl();
        if (urlSpecifiedByUser != null) {
            return Arrays.asList(urlSpecifiedByUser);
        } else {
            return Cast.cast(GitDependency.class, dependency).getUrls();
        }
    }

    private void tryCloneWithUrls(GolangDependency dependency, List<String> urls, Path path) {
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
    protected Optional<Repository> repositoryMatch(Path repoPath, GolangDependency dependency) {
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
