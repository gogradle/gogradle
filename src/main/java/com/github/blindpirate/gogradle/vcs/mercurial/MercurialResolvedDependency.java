package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.VCS_KEY;
import static com.github.blindpirate.gogradle.vcs.mercurial.MercurialNotationDependency.NODE_ID_KEY;
import static com.github.blindpirate.gogradle.vcs.mercurial.MercurialNotationDependency.URL_KEY;

public class MercurialResolvedDependency extends AbstractResolvedDependency {
    private static final int COMMIT_PREFIX_LENGTH = 7;
    private String tag;
    private String repoUrl;

    private MercurialResolvedDependency(String name, String nodeId, long updateTime) {
        super(name, nodeId, updateTime);
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        return ImmutableMap.of(NAME_KEY, getName(),
                VCS_KEY, VcsType.MERCURIAL.getName(),
                URL_KEY, repoUrl,
                NODE_ID_KEY, getVersion());
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
    protected Class<? extends DependencyInstaller> getInstallerClass() {
        return MercurialDependencyManager.class;
    }

    public static MercurialResolvedDependencyBuilder builder() {
        return new MercurialResolvedDependencyBuilder();
    }


    public static final class MercurialResolvedDependencyBuilder {
        private MercurialNotationDependency notationDependency;
        private long commitTime;
        private String name;
        private String tag;
        private String nodeId;
        private String repoUrl;

        private MercurialResolvedDependencyBuilder() {
        }

        public MercurialResolvedDependencyBuilder withNotationDependency(MercurialNotationDependency d) {
            this.notationDependency = d;
            return this;
        }

        public MercurialResolvedDependencyBuilder withCommitTime(long commitTime) {
            this.commitTime = commitTime;
            return this;
        }

        public MercurialResolvedDependencyBuilder withNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public MercurialResolvedDependencyBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MercurialResolvedDependencyBuilder withTag(String tag) {
            this.tag = tag;
            return this;
        }

        public MercurialResolvedDependencyBuilder withRepoUrl(String repoUrl) {
            this.repoUrl = repoUrl;
            return this;
        }

        public MercurialResolvedDependency build() {
            MercurialResolvedDependency ret = new MercurialResolvedDependency(name, nodeId, commitTime);
            ret.repoUrl = this.repoUrl;
            ret.tag = this.tag;

            ret.setFirstLevel(notationDependency.isFirstLevel());
            ret.transitiveDepExclusions = notationDependency.getTransitiveDepExclusions();

            return ret;
        }
    }
}
