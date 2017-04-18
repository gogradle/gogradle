package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency;
import com.github.blindpirate.gogradle.vcs.VcsType;

public class GitResolvedDependency extends VcsResolvedDependency {
    public GitResolvedDependency(String name,
                                 String url,
                                 String commitId,
                                 long commitTime) {
        super(name, url, commitId, commitTime);
    }

    @Override
    public VcsType getVcsType() {
        return VcsType.GIT;
    }

}
