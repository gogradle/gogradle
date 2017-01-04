package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.VCS_KEY;
import static com.github.blindpirate.gogradle.vcs.VcsType.Git;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.COMMIT_KEY;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.URL_KEY;

public class GitResolvedDependency extends AbstractResolvedDependency {
    private String repoUrl;

    private GitResolvedDependency(String name, String commitId, long commitTime) {
        super(name, commitId, commitTime);
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        return ImmutableMap.of(
                NAME_KEY, getName(),
                VCS_KEY, Git.getName(),
                URL_KEY, repoUrl,
                COMMIT_KEY, getVersion());
    }

    public static GitResolvedDependencyBuilder builder() {
        return new GitResolvedDependencyBuilder();
    }


    public static final class GitResolvedDependencyBuilder {
        private NotationDependency notationDependency;
        private String repoUrl;
        private String commitId;
        private long commitTime;
        private String name;

        private GitResolvedDependencyBuilder() {
        }


        public GitResolvedDependencyBuilder withRepoUrl(String repoUrl) {
            this.repoUrl = repoUrl;
            return this;
        }

        public GitResolvedDependencyBuilder withNotationDependency(NotationDependency notationDependency) {
            this.notationDependency = notationDependency;
            return this;
        }

        public GitResolvedDependencyBuilder withCommitId(String commitId) {
            this.commitId = commitId;
            return this;
        }

        public GitResolvedDependencyBuilder withCommitTime(long commitTime) {
            this.commitTime = commitTime;
            return this;
        }

        public GitResolvedDependencyBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public GitResolvedDependency build() {
            GitResolvedDependency ret = new GitResolvedDependency(name, commitId, commitTime);
            ret.repoUrl = this.repoUrl;
            ret.setFirstLevel(notationDependency.isFirstLevel());
            ret.transitiveDepExclusions = notationDependency.getTransitiveDepExclusions();
            return ret;
        }
    }
}
