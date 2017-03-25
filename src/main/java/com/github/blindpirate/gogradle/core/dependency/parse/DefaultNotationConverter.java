package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.vcs.VcsType;

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
        GolangPackage packageInfo = packagePathResolver.produce(packagePath).get();
        if (packageInfo instanceof VcsGolangPackage) {
            return VcsGolangPackage.class.cast(packageInfo).getVcsType();
        } else if (packageInfo instanceof UnrecognizedGolangPackage) {
            // Git is the default vcs
            return VcsType.GIT;
        } else {
            throw new IllegalArgumentException("Cannot covert " + notation);
        }
    }


    private String extractPackagePath(String notation) {
        for (int i = 0; i < notation.length(); ++i) {
            char c = notation.charAt(i);
            if (c == '#' || c == '@') {
                return notation.substring(0, i);
            }
        }
        return notation;
    }
}
