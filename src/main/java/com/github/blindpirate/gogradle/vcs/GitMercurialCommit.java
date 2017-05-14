/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
