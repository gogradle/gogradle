package com.github.blindpirate.gogradle.vcs;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;

public class GitMercurialCommit {
    private String id;
    private String tag;
    private long commitTime;
    private Version semVersion;

    public String getId() {
        return id;
    }

    public long getCommitTime() {
        return commitTime;
    }

    public String getTag() {
        return tag;
    }

    public Version getSemVersion() {
        return semVersion;
    }

    public boolean satisfies(String semVersionExpression) {
        return semVersion != null && semVersion.satisfies(semVersionExpression);
    }

    public static GitMercurialCommit of(String id, long unixMs) {
        return of(id, null, unixMs);
    }

    public static GitMercurialCommit of(String id, String tag, long unixMs) {
        GitMercurialCommit ret = new GitMercurialCommit();
        ret.id = id;
        ret.tag = tag;
        ret.commitTime = unixMs;
        if (tag != null) {
            try {
                ret.semVersion = Version.valueOf(tag);
            } catch (IllegalArgumentException | ParseException e) {
                ret.semVersion = null;
            }
        }
        return ret;
    }
}
