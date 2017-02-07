package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GitNotationDependency extends AbstractNotationDependency {

    public static final String NEWEST_COMMIT = "NEWEST_COMMIT";

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
            return Arrays.asList(url);
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
    public Class<? extends DependencyResolver> getResolverClass() {
        return GitDependencyManager.class;
    }


    @Override
    public String toString() {
        return "GitNotationDependency{"
                + "name='" + getName() + '\''
                + ", commit='" + commit + '\''
                + (tag == null ? "" : ", tag='" + tag + '\'')
                + (url == null ? "" : ", url='" + url + '\'')
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
        GitNotationDependency that = (GitNotationDependency) o;
        return Objects.equals(commit, that.commit)
                && Objects.equals(getName(), that.getName())
                && Objects.equals(isFirstLevel(), that.isFirstLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(commit, getName(), isFirstLevel());
    }
}
