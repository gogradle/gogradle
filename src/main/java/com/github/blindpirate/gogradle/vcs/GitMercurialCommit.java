package com.github.blindpirate.gogradle.vcs;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;

import java.util.regex.Pattern;

public class GitMercurialCommit {
    // v0.3.6 or V1
    private static final Pattern PATTERN_STARTING_WITH_V = Pattern.compile("^[vV]\\d.*");
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
        boolean ret = satisfiesSafely(semVersionExpression);
        if (!ret && PATTERN_STARTING_WITH_V.matcher(semVersionExpression).matches()) {
            return satisfiesSafely(semVersionExpression.substring(1));
        } else {
            return ret;
        }
    }

    private boolean satisfiesSafely(String semVersionExpression) {
        try {
            return semVersion.satisfies(semVersionExpression);
        } catch (Exception e) {
            return false;
        }
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
            ret.semVersion = parseSemVersion(tag);
            if (ret.semVersion == null && PATTERN_STARTING_WITH_V.matcher(tag).matches()) {
                ret.semVersion = parseSemVersion(tag.substring(1));
            }
        }
        return ret;
    }

    private static Version parseSemVersion(String tag) {
        try {
            return Version.valueOf(tag);
        } catch (IllegalArgumentException | ParseException e) {
            return null;
        }
    }
}
