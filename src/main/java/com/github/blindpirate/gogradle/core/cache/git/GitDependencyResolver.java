package com.github.blindpirate.gogradle.core.cache.git;

import com.github.blindpirate.gogradle.core.dependency.GitDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.pack.AbstractDependencyResolver;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.GitUtils;
import com.google.common.base.Optional;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import javax.inject.Inject;
import java.nio.file.Path;

public class GitDependencyResolver extends AbstractDependencyResolver<Repository, String> {

    private static final String DEFAULT_BRANCH = "master";
    @Inject
    private GitUtils gitUtils;

    @Override
    protected void resetToSpecifiedVersion(Repository repository, String commitId) {
        gitUtils.resetToCommit(repository, commitId);
    }

    @Override
    protected String determineVersion(Repository repository, GolangDependency dependency) {
        GitDependency gitDependency = (GitDependency) dependency;
        if (gitDependency.getTag() != null) {
            Optional<String> commit = gitUtils.findCommitByTag(repository, gitDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }

            commit = gitUtils.findCommitBySemVersion(repository, gitDependency.getTag());
            if (commit.isPresent()) {
                return commit.get();
            }
        }

        if (gitDependency.getCommit() != null) {
            Optional<String> commit = gitUtils.findCommit(repository, gitDependency.getCommit());
            return commit.get();
        }

        // use HEAD of master branch
        // TODO the default branch may not be master
        return gitUtils.headCommitOfBranch(repository, DEFAULT_BRANCH);
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
