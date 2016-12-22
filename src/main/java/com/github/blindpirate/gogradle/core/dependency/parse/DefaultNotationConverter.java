package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.pack.PackageInfo;
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver;
import com.github.blindpirate.gogradle.core.pack.PackageResolutionException;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DefaultNotationConverter implements NotationConverter {
    private final PackageNameResolver packageNameResolver;

    @Inject
    public DefaultNotationConverter(PackageNameResolver packageNameResolver) {
        this.packageNameResolver = packageNameResolver;
    }

    @Override
    public Map<String, Object> convert(String notation) {
        VcsType vcs = extractVcs(notation);
        return vcs.getNotationConverter().convert(notation);
    }

    private VcsType extractVcs(String notation) {
        String packageName = extractPackageName(notation);
        Optional<PackageInfo> info = packageNameResolver.produce(packageName);
        if (!info.isPresent()) {
            throw PackageResolutionException.cannotResolveName(packageName);
        }
        return info.get().getVcsType();
    }


    private String extractPackageName(String notation) {
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
