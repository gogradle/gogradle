package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Converts a map notation to a {@link NotationDependency}
 * which can be resolved to a {@link AbstractResolvedDependency}
 */
@Singleton
public class DefaultMapNotationParser implements MapNotationParser {
    private final DirMapNotationParser dirMapNotationParser;
    private final PackagePathResolver packagePathResolver;

    @Inject
    public DefaultMapNotationParser(DirMapNotationParser dirMapNotationParser,
                                    PackagePathResolver packagePathResolver) {
        this.dirMapNotationParser = dirMapNotationParser;
        this.packagePathResolver = packagePathResolver;
    }

    @Override
    public NotationDependency parse(Map<String, Object> notation) {
        Assert.isTrue(notation.containsKey(NAME_KEY), "Name must be specified!");
        if (notation.containsKey(DIR_KEY)) {
            return dirMapNotationParser.parse(notation);
        } else {
            return parseWithVcs(notation);
        }
    }

    private NotationDependency parseWithVcs(Map<String, Object> notation) {
        String packagePath = MapUtils.getString(notation, NAME_KEY);
        GolangPackage golangPackage = packagePathResolver.produce(packagePath).get();
        notation.put(PACKAGE_KEY, golangPackage);

        normalize(notation, golangPackage);

        verifyVcs(notation, golangPackage);

        MapNotationParser parser =
                golangPackage.getVcsType().getService(MapNotationParser.class);

        return parser.parse(notation);
    }

    private void normalize(Map<String, Object> notation, GolangPackage golangPackage) {
        if (golangPackage instanceof VcsGolangPackage) {
            VcsGolangPackage vcsGolangPackage = VcsGolangPackage.class.cast(golangPackage);
            if (!vcsGolangPackage.isRoot()) {
                notation.put(NAME_KEY, vcsGolangPackage.getRootPath());
            }
        }
    }

    private void verifyVcs(Map<String, Object> notation, GolangPackage golangPackage) {
        if (notation.containsKey(VCS_KEY)) {
            String declaredVcs = notation.get(VCS_KEY).toString();
            String actualVcs = golangPackage.getVcsType().getName();
            Assert.isTrue(declaredVcs.equals(actualVcs),
                    "Vcs type not match: declared is " + declaredVcs + " but actual is " + actualVcs);
        }
    }

}
