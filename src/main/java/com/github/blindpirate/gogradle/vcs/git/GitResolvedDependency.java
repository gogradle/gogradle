package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;
import com.github.blindpirate.gogradle.vcs.GitMercurialResolvedDependency;
import com.github.blindpirate.gogradle.vcs.VcsType;

public class GitResolvedDependency extends GitMercurialResolvedDependency {
    public GitResolvedDependency(String name, String commitId, long commitTime) {
        super(name, commitId, commitTime);
    }

    @Override
    protected Class<? extends DependencyInstaller> getInstallerClass() {
        return GitDependencyManager.class;
    }

    @Override
    protected VcsType getVcsType() {
        return VcsType.GIT;
    }
}
