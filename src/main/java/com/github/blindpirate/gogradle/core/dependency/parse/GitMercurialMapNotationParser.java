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

package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.vcs.VcsNotationDependency;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency;
import com.github.blindpirate.gogradle.vcs.mercurial.MercurialNotationDependency;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

import static com.github.blindpirate.gogradle.util.MapUtils.getString;
import static com.github.blindpirate.gogradle.util.StringUtils.allBlank;
import static com.github.blindpirate.gogradle.vcs.VcsNotationDependency.COMMIT_KEY;
import static com.github.blindpirate.gogradle.vcs.VcsNotationDependency.LATEST_COMMIT;
import static com.github.blindpirate.gogradle.vcs.VcsNotationDependency.TAG_KEY;
import static com.github.blindpirate.gogradle.vcs.VcsNotationDependency.URLS_KEY;
import static com.github.blindpirate.gogradle.vcs.VcsNotationDependency.URL_KEY;
import static com.github.blindpirate.gogradle.vcs.VcsNotationDependency.VERSION_KEY;

@Singleton
public class GitMercurialMapNotationParser extends AutoConfigureMapNotationParser<VcsNotationDependency> {
    @Override
    protected void preConfigure(Map<String, Object> notation) {
        String version = getString(notation, VERSION_KEY);
        String tag = getString(notation, TAG_KEY);
        String commit = getString(notation, COMMIT_KEY);

        if (allBlank(version, tag, commit)) {
            notation.put("commit", LATEST_COMMIT);
        }
    }

    @Override
    protected Class<? extends NotationDependency> determineDependencyClass(Map<String, Object> notationMap) {
        VcsType vcsType = getVcsType(notationMap);
        Assert.isTrue(vcsType == VcsType.GIT || vcsType == VcsType.MERCURIAL);
        if (vcsType == VcsType.GIT) {
            return GitNotationDependency.class;
        } else {
            return MercurialNotationDependency.class;
        }
    }

    private VcsType getVcsType(Map<String, Object> notationMap) {
        Optional<VcsType> vcsType = VcsType.of(MapUtils.getString(notationMap, VCS_KEY));
        if (vcsType.isPresent()) {
            return vcsType.get();
        } else {
            VcsGolangPackage pkg = MapUtils.getValue(notationMap, PACKAGE_KEY, VcsGolangPackage.class);
            Assert.isTrue(pkg != null, "Cannot found vcs in " + notationMap);
            return pkg.getVcs();
        }
    }
}
