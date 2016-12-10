package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.ProxyPackageModule;
import com.github.blindpirate.gogradle.core.cache.git.GitDependencyResolver;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;

import static com.github.blindpirate.gogradle.vcs.VcsType.Git;

public class GitDependency extends AbstractDependency {

    public static final String NEWEST_COMMIT = "NEWEST_COMMIT";

    private String url;
    private String commit;
    private String tag;

    @Inject
    private GitDependencyResolver gitDependencyResolver;

    public GitDependency(String name) {
        super(name);
    }

    public String getUrl() {
        return url;
    }

    public String getCommit() {
        return commit;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String getVersion() {
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

    public GitDependency setVersion(String version) {
        this.tag = version;
        return this;
    }


    @Override
    public GolangPackageModule getPackage() {
        GolangPackageModule tmpModule = gitDependencyResolver.resolve(this);
        return new ProxyPackageModule(tmpModule);
    }

    @Override
    public VcsType vcs() {
        return Git;
    }

}
