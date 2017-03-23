package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency;
import com.github.blindpirate.gogradle.vcs.mercurial.MercurialNotationDependency;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

import static com.github.blindpirate.gogradle.util.MapUtils.getString;
import static com.github.blindpirate.gogradle.util.StringUtils.allBlank;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.COMMIT_KEY;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.NEWEST_COMMIT;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.TAG_KEY;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.URLS_KEY;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.VERSION_KEY;

@Singleton
public class GitMercurialMapNotationParser extends AutoConfigureMapNotationParser<GitMercurialNotationDependency> {
    @Override
    protected void preConfigure(Map<String, Object> notation) {
        String version = getString(notation, VERSION_KEY);
        String tag = getString(notation, TAG_KEY);
        String commit = getString(notation, COMMIT_KEY);

        VcsGolangPackage pkg = MapUtils.getValue(notation, PACKAGE_KEY, VcsGolangPackage.class);
        if (pkg != null) {
            notation.put(URLS_KEY, pkg.getUrls());
        }

        if (allBlank(version, tag, commit)) {
            notation.put("commit", NEWEST_COMMIT);
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
            return pkg.getVcsType();
        }
    }
}
