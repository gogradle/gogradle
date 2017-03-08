package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.vcs.VcsAccessor;

import java.io.File;
import java.util.Optional;

public interface MercurialAccessor extends VcsAccessor {
    HgRepository getRepository(File dir);

    String getRemoteUrl(HgRepository repository);

    void resetToSpecificNodeId(HgRepository repository, String version);

    long getLastCommitTimeOfPath(HgRepository repository, String relativePath);

    Optional<HgChangeset> findChangesetByTag(HgRepository repository, String tag);

    Optional<HgChangeset> findChangesetById(HgRepository repository, String nodeId);

    HgChangeset headOfBranch(HgRepository repository, String defaultBranch);

    void pull(HgRepository hgRepository);

    HgRepository cloneWithUrl(File directory, String url);
}
