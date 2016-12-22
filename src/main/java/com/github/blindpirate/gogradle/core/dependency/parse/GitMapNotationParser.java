package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GitDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.pack.PackageInfo;
import com.github.blindpirate.gogradle.util.MapUtils;

import javax.inject.Singleton;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.dependency.GitDependency.COMMIT_KEY;
import static com.github.blindpirate.gogradle.core.dependency.GitDependency.NEWEST_COMMIT;
import static com.github.blindpirate.gogradle.core.dependency.GitDependency.TAG_KEY;
import static com.github.blindpirate.gogradle.core.dependency.GitDependency.URLS_KEY;
import static com.github.blindpirate.gogradle.core.dependency.GitDependency.VERSION_KEY;
import static com.github.blindpirate.gogradle.util.MapUtils.getString;
import static com.github.blindpirate.gogradle.util.StringUtils.allBlank;

@Singleton
public class GitMapNotationParser extends AutoConfigureMapNotationParser {

    @Override
    protected void preConfigure(Map<String, Object> notation) {
        String version = getString(notation, VERSION_KEY);
        String tag = getString(notation, TAG_KEY);
        String commit = getString(notation, COMMIT_KEY);

        PackageInfo info = MapUtils.getValue(notation, INFO_KEY, PackageInfo.class);
        if (info != null) {
            notation.put(URLS_KEY, info.getUrls());
        }

        if (allBlank(version, tag, commit)) {
            notation.put("commit", NEWEST_COMMIT);
        }
    }

    @Override
    protected Class<? extends GolangDependency> determineDependencyClass(Map<String, Object> notationMap) {
        return GitDependency.class;
    }

}
