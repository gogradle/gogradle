/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.vcs;

import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager;
import com.github.blindpirate.gogradle.util.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.SUBPACKAGES_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.VCS_KEY;
import static com.github.blindpirate.gogradle.vcs.VcsNotationDependency.URLS_KEY;
import static com.github.blindpirate.gogradle.vcs.VcsNotationDependency.URL_KEY;

public class VcsResolvedDependency extends AbstractResolvedDependency {
    private static final int COMMIT_PREFIX_LENGTH = 7;
    private String tag;

    private VcsResolvedDependency(String name,
                                  String commitId,
                                  long commitTime) {
        super(name, commitId, commitTime);
    }

    public VcsType getVcsType() {
        return getPackage().getVcsType();
    }

    @Override
    protected DependencyManager getInstaller() {
        return getVcsType().getService(DependencyManager.class);
    }

    // NOTE: Don't modify it if unnecessary. It's part of cross-version protocol
    @Override
    public Map<String, Object> toLockedNotation() {
        Map<String, Object> ret = MapUtils.asMap(
                NAME_KEY, getName(),
                VCS_KEY, getVcsType().getName(),
                VcsNotationDependency.COMMIT_KEY, getVersion());
        if (getUrls().size() == 1) {
            ret.put(URL_KEY, getUrls().get(0));
        } else {
            ret.put(URLS_KEY, getUrls());
        }
        if (!containsAllSubpackages()) {
            ret.put(SUBPACKAGES_KEY, new ArrayList<>(getSubpackages()));
        }
        return ret;
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
    public String toString() {
        return getName() + "#" + getVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        VcsResolvedDependency that = (VcsResolvedDependency) o;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getVcsType(), that.getVcsType())
                && Objects.equals(getVersion(), that.getVersion())
                && Objects.equals(getUrls(), that.getUrls());
    }

    private List<String> getUrls() {
        return getPackage().getRepository().getUrls();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getVcsType(), getVersion(), getUrls());
    }

    @Override
    public VcsGolangPackage getPackage() {
        return (VcsGolangPackage) super.getPackage();
    }

    public static GitMercurialResolvedDependencyBuilder builder() {
        return new GitMercurialResolvedDependencyBuilder();
    }

    public static final class GitMercurialResolvedDependencyBuilder {
        private VcsNotationDependency notationDependency;
        private String commitId;
        private long commitTime;

        private GitMercurialResolvedDependencyBuilder() {
        }

        public GitMercurialResolvedDependencyBuilder withNotationDependency(NotationDependency notationDependency) {
            this.notationDependency = (VcsNotationDependency) notationDependency;
            return this;
        }

        public GitMercurialResolvedDependencyBuilder withCommitId(String commitId) {
            this.commitId = commitId;
            return this;
        }

        public GitMercurialResolvedDependencyBuilder withCommitTime(long commitTime) {
            this.commitTime = commitTime;
            return this;
        }

        public VcsResolvedDependency build() {
            VcsResolvedDependency ret;
            ret = new VcsResolvedDependency(notationDependency.getName(), commitId, commitTime);
            ret.tag = notationDependency.getTag();
            ret.setPackage(notationDependency.getPackage());
            ret.setSubpackages(notationDependency.getSubpackages());
            ret.setFirstLevel(notationDependency.isFirstLevel());
            return ret;
        }
    }
}
