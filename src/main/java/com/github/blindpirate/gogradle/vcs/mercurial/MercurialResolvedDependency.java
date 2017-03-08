package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;
import com.github.blindpirate.gogradle.vcs.GitMercurialResolvedDependency;
import com.github.blindpirate.gogradle.vcs.VcsType;

public class MercurialResolvedDependency extends GitMercurialResolvedDependency {
    public MercurialResolvedDependency(String name, String commitId, long commitTime) {
        super(name, commitId, commitTime);
    }

    @Override
    protected Class<? extends DependencyInstaller> getInstallerClass() {
        return MercurialDependencyManager.class;
    }

    @Override
    protected VcsType getVcsType() {
        return VcsType.MERCURIAL;
    }
}
