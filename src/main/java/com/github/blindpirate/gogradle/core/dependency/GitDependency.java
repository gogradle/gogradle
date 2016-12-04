package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.vcs.Vcs;
import com.github.zafarkhaja.semver.Version;

import static com.github.blindpirate.gogradle.core.vcs.Vcs.Git;

public class GitDependency extends ScmDependency {

    private String url;
    private String commit;
    private String tag;

    private Version semVersion;

    public Version getSemVersion() {
        return semVersion;
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

    public static GitDependencyBuilder builder() {
        return new GitDependencyBuilder();
    }

    @Override
    public Vcs vcs() {
        return Git;
    }

    public static final class GitDependencyBuilder {
        private String name;
        private String url;
        private String commit;
        private String tag;
        private Version semVersion;

        private GitDependencyBuilder() {
        }


        public GitDependencyBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public GitDependencyBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public GitDependencyBuilder withCommit(String commit) {
            this.commit = commit;
            return this;
        }

        public GitDependencyBuilder withTag(String tag) {
            this.tag = tag;
            return this;
        }

        public GitDependencyBuilder withSemVersion(Version semVersion) {
            this.semVersion = semVersion;
            return this;
        }

        public GitDependency build() {
            GitDependency gitDependency = new GitDependency(url, commit, tag);
            gitDependency.setName(this.name);
            gitDependency.semVersion = this.semVersion;
            return gitDependency;
        }
    }
}
