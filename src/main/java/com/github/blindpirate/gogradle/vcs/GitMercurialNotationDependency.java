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

import com.github.blindpirate.gogradle.core.cache.CacheScope;
import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;

public abstract class GitMercurialNotationDependency extends AbstractNotationDependency {

    public static final String LATEST_COMMIT = "LATEST_COMMIT";

    public static final String URL_KEY = "url";
    public static final String URLS_KEY = "urls";
    public static final String COMMIT_KEY = "commit";
    // not implemented yet
    public static final String BRANCH_KEY = "branch";
    public static final String TAG_KEY = "tag";
    private String commit;
    private String tag;
    // url specified by user
    private String url;
    // urls auto injected, e.g. https://github.com/a/b.git or git@github.com:a/b.git
    private List<String> urls = new ArrayList<>();

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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getUrls() {
        if (StringUtils.isNotBlank(url)) {
            return singletonList(url);
        } else {
            return urls;
        }
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
        if (StringUtils.isNotBlank(commit) && !LATEST_COMMIT.equals(commit)) {
            return CacheScope.PERSISTENCE;
        } else {
            return CacheScope.BUILD;
        }
    }

    public boolean isLatest() {
        return LATEST_COMMIT.equals(commit);
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
        GitMercurialNotationDependency that = (GitMercurialNotationDependency) o;
        return Objects.equals(commit, that.commit)
                && Objects.equals(getUrls(), that.getUrls());
    }

    @Override
    public int hashCode() {
        return Objects.hash(commit, getUrls(), super.hashCode());
    }
}
