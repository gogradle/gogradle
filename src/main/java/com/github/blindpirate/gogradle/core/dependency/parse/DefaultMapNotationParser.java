package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage;
import com.github.blindpirate.gogradle.core.ResolvableGolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.UnrecognizedPackageNotationDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static java.util.Collections.singletonList;

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

        String packagePath = MapUtils.getString(notation, NAME_KEY);
        GolangPackage pkg = packagePathResolver.produce(packagePath).get();

        if (pkg instanceof ResolvableGolangPackage) {
            String rootPathString = ResolvableGolangPackage.class.cast(pkg).getRootPathString();
            notation.put(NAME_KEY, rootPathString);
            notation.put(PACKAGE_KEY, ResolvableGolangPackage.class.cast(pkg).resolve(rootPathString).get());
        } else {
            notation.put(PACKAGE_KEY, pkg);
        }

        if (notation.containsKey(DIR_KEY) || pkg instanceof LocalDirectoryGolangPackage) {
            return dirMapNotationParser.parse(notation);
        } else if (notation.containsKey(VENDOR_PATH_KEY)) {
            return vendorMapNotationParser.parse(notation);
        } else {
            return parseWithVcs(notation, pkg);
        }
    }

    private NotationDependency parseWithVcs(Map<String, Object> notation, GolangPackage pkg) {
        if (pkg instanceof VcsGolangPackage) {
            return parseVcsPackage(notation, (VcsGolangPackage) pkg);
        } else if (pkg instanceof UnrecognizedGolangPackage) {
            return parseUnrecognizedPackage(notation, (UnrecognizedGolangPackage) pkg);
        } else {
            throw DependencyResolutionException.cannotParseNotation(notation);
        }
    }

    private NotationDependency parseUnrecognizedPackage(Map<String, Object> notation,
                                                        UnrecognizedGolangPackage pkg) {
        String url = MapUtils.getString(notation, GitMercurialNotationDependency.URL_KEY);
        if (url == null) {
            return UnrecognizedPackageNotationDependency.of(pkg);
        } else {
            VcsGolangPackage vcsPkg = adaptAsVcsPackage(notation, pkg, url);
            notation.put(PACKAGE_KEY, vcsPkg);
            return parseVcsPackage(notation, vcsPkg);
        }
    }

    private VcsGolangPackage adaptAsVcsPackage(Map<String, Object> notation,
                                               UnrecognizedGolangPackage pkg,
                                               String url) {
        return VcsGolangPackage.builder()
                .withSubstitutedVcsInfo(determineVcs(notation), singletonList(url))
                .withPath(pkg.getPath())
                .withRootPath(pkg.getPath())
                .build();
    }

    private NotationDependency parseVcsPackage(Map<String, Object> notation, VcsGolangPackage pkg) {
        verifyVcs(notation, pkg);

        return pkg.getVcsType().getService(MapNotationParser.class).parse(notation);
    }

    private VcsType determineVcs(Map<String, Object> notation) {
        String vcs = MapUtils.getString(notation, VCS_KEY);
        if (vcs == null) {
            notation.put(VCS_KEY, VcsType.GIT.getName());
            return VcsType.GIT;
        } else {
            return VcsType.of(vcs).get();
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
