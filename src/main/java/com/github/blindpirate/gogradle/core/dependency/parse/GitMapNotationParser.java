package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency;

import javax.inject.Singleton;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.MapUtils.getString;
import static com.github.blindpirate.gogradle.util.StringUtils.allBlank;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.COMMIT_KEY;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.NEWEST_COMMIT;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.TAG_KEY;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.URL_KEY;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.VERSION_KEY;

@Singleton
public class GitMapNotationParser extends AutoConfigureMapNotationParser<GitNotationDependency> {
    @Override
    protected void preConfigure(Map<String, Object> notation) {
        String version = getString(notation, VERSION_KEY);
        String tag = getString(notation, TAG_KEY);
        String commit = getString(notation, COMMIT_KEY);

        GolangPackage info = MapUtils.getValue(notation, PACKAGE_KEY, GolangPackage.class);
        if (info != null) {
            notation.put(URL_KEY, info.getUrl());
        }

        if (allBlank(version, tag, commit)) {
            notation.put("commit", NEWEST_COMMIT);
        }
    }
}
