package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.vcs.git.GitResolvedDependency;
import com.github.blindpirate.gogradle.vcs.mercurial.MercurialResolvedDependency;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Objects;

import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.VCS_KEY;

public abstract class VcsResolvedDependency extends AbstractResolvedDependency {
    private static final int COMMIT_PREFIX_LENGTH = 7;
    private String tag;
    private String url;

    protected VcsResolvedDependency(String name,
                                    String url,
                                    String commitId,
                                    long commitTime) {
        super(name, commitId, commitTime);
        this.url = url;
    }

    public abstract VcsType getVcsType();

    public String getUrl() {
        return url;
    }

    @Override
    protected DependencyInstaller getInstaller() {
        return getVcsType().getService(DependencyInstaller.class);
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        return ImmutableMap.of(
                NAME_KEY, getName(),
                VCS_KEY, getVcsType().getName(),
                GitMercurialNotationDependency.URL_KEY, getUrl(),
                GitMercurialNotationDependency.COMMIT_KEY, getVersion());
    }

    @Override
    public String formatVersion() {
        if (tag != null) {
            return tag + "(" + getVersion().substring(0, COMMIT_PREFIX_LENGTH) + ")";
        } else {
            return getVersion().substring(0, COMMIT_PREFIX_LENGTH);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VcsResolvedDependency that = (VcsResolvedDependency) o;
        return Objects.equals(getVersion(), that.getVersion())
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getVcsType(), that.getVcsType())
                && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, getVersion(), getName(), getVcsType());
    }

    public static GitMercurialResolvedDependencyBuilder builder(VcsType vcsType) {
        return new GitMercurialResolvedDependencyBuilder(vcsType);
    }

    public static final class GitMercurialResolvedDependencyBuilder {
        private VcsType vcsType;
        private NotationDependency notationDependency;
        private String url;
        private String tag;
        private String commitId;
        private long commitTime;
        private String name;

        private GitMercurialResolvedDependencyBuilder(VcsType vcsType) {
            this.vcsType = vcsType;
        }

        public GitMercurialResolvedDependencyBuilder withUrl(String repoUrl) {
            this.url = repoUrl;
            return this;
        }

        public GitMercurialResolvedDependencyBuilder withNotationDependency(NotationDependency notationDependency) {
            this.notationDependency = notationDependency;
            return this;
        }

        public GitMercurialResolvedDependencyBuilder withCommitId(String commitId) {
            this.commitId = commitId;
            return this;
        }

        public GitMercurialResolvedDependencyBuilder withTag(String tag) {
            this.tag = tag;
            return this;
        }

        public GitMercurialResolvedDependencyBuilder withCommitTime(long commitTime) {
            this.commitTime = commitTime;
            return this;
        }

        public GitMercurialResolvedDependencyBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public VcsResolvedDependency build() {
            VcsResolvedDependency ret;
            Assert.isTrue(vcsType == VcsType.GIT || vcsType == VcsType.MERCURIAL);
            if (vcsType == VcsType.GIT) {
                ret = new GitResolvedDependency(name, url, commitId, commitTime);
            } else {
                ret = new MercurialResolvedDependency(name, url, commitId, commitTime);
            }
            ret.tag = this.tag;
            ret.setFirstLevel(notationDependency.isFirstLevel());
            ret.setPackage(notationDependency.getPackage());
            return ret;
        }
    }
}
