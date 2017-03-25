package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.GolangRepositoryHandler;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.github.blindpirate.gogradle.vcs.git.GolangRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Converts a map notation to a {@link NotationDependency}
 * which can be resolved to a {@link AbstractResolvedDependency}
 */
@Singleton
public class DefaultMapNotationParser implements MapNotationParser {
    private final DirMapNotationParser dirMapNotationParser;
    private final VendorMapNotationParser vendorMapNotationParser;
    private final PackagePathResolver packagePathResolver;
    private final GolangRepositoryHandler repositoryHandler;

    @Inject
    public DefaultMapNotationParser(DirMapNotationParser dirMapNotationParser,
                                    VendorMapNotationParser vendorMapNotationParser,
                                    PackagePathResolver packagePathResolver,
                                    GolangRepositoryHandler repositoryHandler) {
        this.dirMapNotationParser = dirMapNotationParser;
        this.vendorMapNotationParser = vendorMapNotationParser;
        this.packagePathResolver = packagePathResolver;
        this.repositoryHandler = repositoryHandler;
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

        GolangPackage pkg = packagePathResolver.produce(packagePath).get();

        if (pkg instanceof VcsGolangPackage) {
            VcsGolangPackage vcsGolangPackage = (VcsGolangPackage) pkg;
            NotationDependency ret = parseVcsPackage(notation, vcsGolangPackage);
            substituteUrls(ret, vcsGolangPackage);
            return ret;
        } else if (pkg instanceof UnrecognizedGolangPackage) {
            VcsGolangPackage vcsGolangPackage = adaptAsVcsPackage(notation,
                    UnrecognizedGolangPackage.class.cast(pkg));
            return parseVcsPackage(notation, vcsGolangPackage);
        } else {
            throw DependencyResolutionException.cannotParseNotation(notation);
        }
    }

    private VcsGolangPackage adaptAsVcsPackage(Map<String, Object> notation, UnrecognizedGolangPackage pkg) {
        Optional<String> url = tryGetUrl(notation, pkg);
        Assert.isTrue(url.isPresent(), "Cannot get url of dependency: " + pkg.getPathString());

        return VcsGolangPackage.builder()
                .withVcsType(determineVcs(notation))
                .withPath(pkg.getPath())
                .withRootPath(pkg.getPath())
                .withUrl(url.get())
                .build();

    }

    private Optional<String> tryGetUrl(Map<String, Object> notation, UnrecognizedGolangPackage pkg) {
        String ret = MapUtils.getString(notation, GitMercurialNotationDependency.URL_KEY);
        if (ret != null) {
            return Optional.of(ret);
        } else {
            GolangRepository matchedRepo = repositoryHandler.findMatchedRepository(pkg.getPathString());
            ret = matchedRepo.substitute(pkg.getPathString(), null);
            if (ret != null) {
                return Optional.of(ret);
            }
        }
        return Optional.empty();
    }

    private NotationDependency parseVcsPackage(Map<String, Object> notation, VcsGolangPackage pkg) {
        notation.put(PACKAGE_KEY, pkg);
        normalize(notation, pkg);
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

    private void substituteUrls(NotationDependency vcsDependency, VcsGolangPackage pkg) {
        GitMercurialNotationDependency dependency = (GitMercurialNotationDependency) vcsDependency;

        GolangRepository repository = repositoryHandler.findMatchedRepository(dependency.getName());
        List<String> substitutedUrls = pkg.getUrls().stream()
                .map(url -> repository.substitute(dependency.getName(), url))
                .collect(Collectors.toList());

        dependency.setUrls(substitutedUrls);
    }

    private void normalize(Map<String, Object> notation, VcsGolangPackage pkg) {
        if (!pkg.isRoot()) {
            notation.put(NAME_KEY, pkg.getRootPathString());
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
