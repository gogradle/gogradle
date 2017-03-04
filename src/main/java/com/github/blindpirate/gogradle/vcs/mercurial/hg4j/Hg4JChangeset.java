package com.github.blindpirate.gogradle.vcs.mercurial.hg4j;

import com.github.blindpirate.gogradle.vcs.mercurial.HgChangeset;

public class Hg4JChangeset implements HgChangeset {

    private org.tmatesoft.hg.core.HgChangeset changeset;

    public static Hg4JChangeset fromUnderlying(org.tmatesoft.hg.core.HgChangeset underlying) {
        Hg4JChangeset ret = new Hg4JChangeset();
        ret.changeset = underlying;
        return ret;
    }

    @Override
    public String getId() {
        return changeset.getNodeid().toString();
    }

    @Override
    public long getCommitTime() {
        return changeset.getDate().getRawTime();
    }
}
