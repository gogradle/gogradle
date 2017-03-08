package com.github.blindpirate.gogradle.vcs.mercurial.client;

import com.github.blindpirate.gogradle.vcs.mercurial.HgChangeset;

public class HgClientChangeset implements HgChangeset {

    private String id;
    private long commitTime;

    public static HgClientChangeset of(String id, long commitTime) {
        HgClientChangeset ret = new HgClientChangeset();
        ret.id = id;
        ret.commitTime = commitTime;
        return ret;
    }

    private HgClientChangeset() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getCommitTime() {
        return commitTime;
    }
}
