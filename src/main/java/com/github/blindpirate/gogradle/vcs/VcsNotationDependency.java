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

import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.cache.CacheScope;
import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.util.List;
import java.util.Objects;

public abstract class VcsNotationDependency extends AbstractNotationDependency {

    public static final String LATEST_COMMIT = "LATEST_COMMIT";

    public static final String URL_KEY = "url";
    public static final String URLS_KEY = "urls";
    public static final String BRANCH_KEY = "branch";
    public static final String TAG_KEY = "tag";
    public static final String COMMIT_KEY = "commit";
    private String commit;
    private String tag;

    public String getCommit() {
        return commit;
    }

    public String getTag() {
        return tag;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    // branch is a special tag
    public String getBranch() {
        return tag;
    }

    public void setBranch(String branch) {
        this.tag = branch;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getUrls() {
        return VcsGolangPackage.class.cast(getPackage()).getUrls();
    }

    public void setVersion(String version) {
        this.commit = version;
    }

    @Override
    public String getVersion() {
        return commit;
    }

    @Override
    public CacheScope getCacheScope() {
        if (StringUtils.isNotBlank(getCommit()) && !LATEST_COMMIT.equals(getCommit())) {
            return CacheScope.PERSISTENCE;
        } else {
            return CacheScope.BUILD;
        }
    }

    public boolean isLatest() {
        return LATEST_COMMIT.equals(getCommit());
    }

    @Override
    public String toString() {
        String ret = getName() + ':'
                + (commit == null ? "" : " commit='" + commit + "',")
                + (tag == null ? "" : " tag/branch='" + tag + "',")
                + (getUrls() == null ? "" : " urls=" + getUrls() + ",")
                + (containsAllSubpackages() ? "" : " subpackages='" + getSubpackages() + "',");
        return ret.substring(0, ret.length() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        VcsNotationDependency that = (VcsNotationDependency) o;
        return Objects.equals(commit, that.commit)
                && Objects.equals(getUrls(), that.getUrls());
    }

    @Override
    public int hashCode() {
        return Objects.hash(commit, getUrls(), super.hashCode());
    }
}
