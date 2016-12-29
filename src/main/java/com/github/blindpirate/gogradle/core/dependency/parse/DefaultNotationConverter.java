package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.pack.PackageInfo;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.PackageResolutionException;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.vcs.VcsType;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DefaultNotationConverter implements NotationConverter {
    private final PackagePathResolver packagePathResolver;

    @Inject
    public DefaultNotationConverter(PackagePathResolver packagePathResolver) {
        this.packagePathResolver = packagePathResolver;
    }

    @Override
    @DebugLog
    public Map<String, Object> convert(String notation) {
        VcsType vcs = extractVcs(notation);
        return vcs.getNotationConverter().convert(notation);
    }

    private VcsType extractVcs(String notation) {
        String packagePath = extractPackagePath(notation);
        Optional<PackageInfo> info = packagePathResolver.produce(packagePath);
        if (!info.isPresent()) {
            throw PackageResolutionException.cannotResolvePath(packagePath);
        }
        Assert.isTrue(!info.get().isStandard(), "Cannot convert a standard package!");
        return info.get().getVcsType();
    }


    private String extractPackagePath(String notation) {
        for (int i = 0; i < notation.length(); ++i) {
            char c = notation.charAt(i);
            // TODO not accurate
            if (c == '#' || c == '@') {
                return notation.substring(0, i);
            }
        }
        return notation;
    }
}
