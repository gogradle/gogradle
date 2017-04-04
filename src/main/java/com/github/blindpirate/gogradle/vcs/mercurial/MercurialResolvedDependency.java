package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency;
import com.github.blindpirate.gogradle.vcs.VcsType;

public class MercurialResolvedDependency extends VcsResolvedDependency {
    public MercurialResolvedDependency(String name, String url, String commitId, long commitTime) {
        super(name, url, commitId, commitTime);
    }

    @Override
    public VcsType getVcsType() {
        return VcsType.MERCURIAL;
    }
}
