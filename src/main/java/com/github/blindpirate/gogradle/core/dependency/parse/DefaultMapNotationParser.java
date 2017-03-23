package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.StringUtils;

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
    private final VendorMapNotationParser vendorMapNotationParser;
    private final PackagePathResolver packagePathResolver;

    @Inject
    public DefaultMapNotationParser(DirMapNotationParser dirMapNotationParser,
                                    VendorMapNotationParser vendorMapNotationParser,
                                    PackagePathResolver packagePathResolver) {
        this.dirMapNotationParser = dirMapNotationParser;
        this.vendorMapNotationParser = vendorMapNotationParser;
        this.packagePathResolver = packagePathResolver;
    }

    @Override
    public NotationDependency parse(Map<String, Object> notation) {
        Assert.isTrue(notation.containsKey(NAME_KEY), "Name must be specified!");
        if (notation.containsKey(DIR_KEY)) {
            return dirMapNotationParser.parse(notation);
        } else if (notation.containsKey(VENDOR_PATH_KEY)) {
            return vendorMapNotationParser.parse(notation);
        } else {
            return parseWithVcs(notation);
        }
    }

    private NotationDependency parseWithVcs(Map<String, Object> notation) {
        String packagePath = MapUtils.getString(notation, NAME_KEY);
        GolangPackage golangPackage = packagePathResolver.produce(packagePath).get();

        Assert.isTrue(golangPackage instanceof VcsGolangPackage);
        VcsGolangPackage vcsGolangPackage = (VcsGolangPackage) golangPackage;

        notation.put(PACKAGE_KEY, vcsGolangPackage);
        normalize(notation, vcsGolangPackage);
        verifyVcs(notation, vcsGolangPackage);

        MapNotationParser parser =
                vcsGolangPackage.getVcsType().getService(MapNotationParser.class);

        return parser.parse(notation);
    }

    private void normalize(Map<String, Object> notation, VcsGolangPackage pkg) {
        if (!pkg.isRoot()) {
            notation.put(NAME_KEY, pkg.getRootPath());
        }
    }

    private void verifyVcs(Map<String, Object> notation, VcsGolangPackage pkg) {
        String declaredVcs = MapUtils.getString(notation, VCS_KEY);
        if (StringUtils.isNotBlank(declaredVcs)) {
            String actualVcs = pkg.getVcsType().getName();
            Assert.isTrue(declaredVcs.equals(actualVcs),
                    "Vcs type not match: declared is " + declaredVcs + " but actual is " + actualVcs);
        }
    }

}
