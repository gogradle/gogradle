package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

import java.util.List;

// TODO what about branch?
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
    // urls in GolangPackage e.g https:// and git+ssh://
    private List<String> urls;

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

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersion(String version) {
        this.commit = version;
    }

    @Override
    public String getVersion() {
        return commit;
    }

    @Override
    protected Class<? extends DependencyResolver> resolverClass() {
        return GitDependencyResolver.class;
    }


}
