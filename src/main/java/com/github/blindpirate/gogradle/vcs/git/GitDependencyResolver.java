package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.VcsTempFileModule;
import com.github.blindpirate.gogradle.core.dependency.GitDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.core.pack.AbstractVcsResolver;
import com.github.blindpirate.gogradle.util.Cast;
import com.github.blindpirate.gogradle.util.GitUtils;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.blindpirate.gogradle.core.dependency.GitDependency.COMMIT_KEY;
import static com.github.blindpirate.gogradle.core.dependency.GitDependency.URLS_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;

@Singleton
public class GitDependencyResolver extends AbstractVcsResolver<Repository, RevCommit> {

    public static final String DEFAULT_BRANCH = "master";

    @Inject
    private GitUtils gitUtils;

    @Override
    protected GolangPackageModule createModule(GolangDependency dependency,
                                               Path path,
                                               Repository repository,
                                               RevCommit commit) {
        GitDependency gitDependency = (GitDependency) dependency;
        Map<String, String> lockedNotation = ImmutableMap.of(
                NAME_KEY, gitDependency.getName(),
                URLS_KEY, gitUtils.getRemoteUrl(repository),
                COMMIT_KEY, commit.getName()
        );
        return new VcsTempFileModule(dependency.getName(),
                path,
                toMilliseconds(commit.getCommitTime()),
                lockedNotation);
    }

    @Override
    protected void resetToSpecificVersion(Repository repository, RevCommit commit) {
        gitUtils.resetToCommit(repository, commit.getId().toString());
    }

    @Override
    protected RevCommit determineVersion(Repository repository, GolangDependency dependency) {
        GitDependency gitDependency = (GitDependency) dependency;
        if (gitDependency.getTag() != null) {
            Optional<RevCommit> commit = gitUtils.findCommitByTag(repository, gitDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }

            commit = gitUtils.findCommitBySemVersion(repository, gitDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }
        }

        if (gitDependency.getCommit() != null) {
            Optional<RevCommit> commit = gitUtils.findCommit(repository, gitDependency.getCommit());
            return commit.get();
        }

        // use HEAD of master branch
        // TODO the default branch may not be master
        return gitUtils.headCommitOfBranch(repository, DEFAULT_BRANCH).get();
    }

    @Override
    protected Repository updateRepository(Repository repository, Path path) {
        return gitUtils.hardResetAndUpdate(repository);
    }

    @Override
    protected Repository initRepository(GolangDependency dependency, Path path) {
        List<String> urls = determineUrls(dependency);
        tryCloneWithEveryUrl(dependency, urls, path);
        return gitUtils.getRepository(path);
    }

    private List<String> determineUrls(GolangDependency dependency) {
        String urlSpecifiedByUser = Cast.cast(GitDependency.class, dependency).getUrl();
        if (urlSpecifiedByUser != null) {
            return Arrays.asList(urlSpecifiedByUser);
        } else {
            return Cast.cast(GitDependency.class, dependency).getUrls();
        }
    }

    private void tryCloneWithEveryUrl(GolangDependency dependency, List<String> urls, Path path) {
        for (int i = 0; i < urls.size(); ++i) {
            try {
                gitUtils.cloneWithUrl(urls.get(i), path);
            } catch (Throwable e) {
                // ignore
                // TODO Logger.debug
                if (i == urls.size() - 1) {
                    throw new DependencyResolutionException("Cannot clone git dependency:"
                            + dependency.getName());
                }
            }
        }
    }

    @Override
    protected Optional<Repository> repositoryMatch(Path repoPath, GolangDependency dependency) {
        Repository repository = gitUtils.getRepository(repoPath);
        List<String> urls = determineUrls(dependency);
        Set<String> remoteUrls = gitUtils.getRemoteUrls(repository);

        if (Collections.disjoint(urls, remoteUrls)) {
            return Optional.absent();
        } else {
            return Optional.of(repository);
        }
    }
}
