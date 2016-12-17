package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.pack.DependencyResolver;
import com.github.blindpirate.gogradle.vcs.git.GitDependencyResolver;

public class GitDependency extends AbstractNotationDependency {

    public static final String NEWEST_COMMIT = "NEWEST_COMMIT";

    public static final String URL_KEY = "url";
    public static final String COMMIT_KEY = "commit";
    public static final String BRANCH_KEY = "branch";
    public static final String TAG_KEY = "tag";
    private String url;
    private String commit;
    private String tag;

    public String getUrl() {
        return url;
    }

    public String getCommit() {
        return commit;
    }

    public String getTag() {
        return tag;
    }

    public GitDependency setUrl(String url) {
        this.url = url;
        return this;
    }

    public GitDependency setCommit(String commit) {
        this.commit = commit;
        return this;
    }

    public GitDependency setTag(String tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public String getVersion() {
        return tag;
    }


    public GitDependency setVersion(String version) {
        this.tag = version;
        return this;
    }

    @Override
    protected Class<? extends DependencyResolver> resolverClass() {
        return GitDependencyResolver.class;
    }


}
