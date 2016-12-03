package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;

public class GitDependency extends ScmDependency {

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

    public GitDependency(String url, String commit, String tag) {
        super(null);
        this.url = url;
        this.commit = commit;
        this.tag = tag;
    }

    @Override
    public String getVersion() {
        return commit;
    }

    @Override
    public GolangPackageModule getPackage() {
        return null;
    }
}
