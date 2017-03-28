package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.vcs.git.GitResolvedDependency;
import com.github.blindpirate.gogradle.vcs.mercurial.MercurialResolvedDependency;
import com.google.common.collect.ImmutableMap;

import java.util.HashSet;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.VCS_KEY;

public abstract class GitMercurialResolvedDependency extends AbstractResolvedDependency {
    private static final int COMMIT_PREFIX_LENGTH = 7;
    private String tag;
    private String repoUrl;

    protected GitMercurialResolvedDependency(String name, String commitId, long commitTime) {
        super(name, commitId, commitTime);
    }

    protected abstract VcsType getVcsType();

    @Override
    public Map<String, Object> toLockedNotation() {
        return ImmutableMap.of(
                NAME_KEY, getName(),
                VCS_KEY, getVcsType().getName(),
                GitMercurialNotationDependency.URL_KEY, repoUrl,
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

    public static GitMercurialResolvedDependencyBuilder gitBuilder() {
        return new GitMercurialResolvedDependencyBuilder(VcsType.GIT);
    }

    public static GitMercurialResolvedDependencyBuilder mercurialBuilder() {
        return new GitMercurialResolvedDependencyBuilder(VcsType.MERCURIAL);
    }

    public static final class GitMercurialResolvedDependencyBuilder {
        private VcsType vcsType;
        private NotationDependency notationDependency;
        private String repoUrl;
        private String tag;
        private String commitId;
        private long commitTime;
        private String name;

        private GitMercurialResolvedDependencyBuilder(VcsType vcsType) {
            this.vcsType = vcsType;
        }

        public GitMercurialResolvedDependencyBuilder withRepoUrl(String repoUrl) {
            this.repoUrl = repoUrl;
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

        public GitMercurialResolvedDependency build() {
            GitMercurialResolvedDependency ret = null;
            Assert.isTrue(vcsType == VcsType.GIT || vcsType == VcsType.MERCURIAL);
            if (vcsType == VcsType.GIT) {
                ret = new GitResolvedDependency(name, commitId, commitTime);
            } else {
                ret = new MercurialResolvedDependency(name, commitId, commitTime);
            }
            ret.repoUrl = this.repoUrl;
            ret.tag = this.tag;
            ret.setFirstLevel(notationDependency.isFirstLevel());
            ret.transitiveDepExclusions = new HashSet<>(notationDependency.getTransitiveDepExclusions());
            return ret;
        }
    }
}
