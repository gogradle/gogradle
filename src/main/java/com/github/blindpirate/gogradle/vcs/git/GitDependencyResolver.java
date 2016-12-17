package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.VcsTempFileModule;
import com.github.blindpirate.gogradle.core.dependency.GitDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
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
import java.util.Map;

import static com.github.blindpirate.gogradle.core.dependency.GitDependency.*;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.util.DateUtils.*;

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
                URL_KEY, gitDependency.getUrl(),
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
        gitUtils.cloneWithUrl(((GitDependency) dependency).getUrl(), path);
        return gitUtils.getRepository(path);
    }

    @Override
    protected Optional<Repository> repositoryMatch(Path repoPath, GolangDependency dependency) {
        Repository repository = gitUtils.getRepository(repoPath);

        // TODO git@github.com:a/b.git and https://github.com/a/b.git
        if (gitUtils.getRemoteUrl(repository).contains(((GitDependency) dependency).getUrl())) {
            return Optional.of(repository);
        } else {
            return Optional.absent();
        }
    }
}
