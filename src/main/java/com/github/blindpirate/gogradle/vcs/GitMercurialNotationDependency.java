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
        if (StringUtils.isBlank(commit) || LATEST_COMMIT.equals(commit)) {
            return CacheScope.BUILD;
        } else {
            return CacheScope.PERSISTENCE;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
                + "name='" + getName() + '\''
                + (commit == null ? "" : ", commit='" + commit + '\'')
                + (tag == null ? "" : ", tag='" + tag + '\'')
                + (getUrls() == null ? "" : ", urls='" + getUrls() + '\'')
                + '}';
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
