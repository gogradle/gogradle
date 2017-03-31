package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.GolangRepositoryHandler;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.UnrecognizedPackageNotationDependency;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.StringUtils.isNotBlank;

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

    private final Map<Path, NotationDependency> unresolvedPackageResultCache = new HashMap<>();

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

        String packagePath = MapUtils.getString(notation, NAME_KEY);
        GolangPackage pkg = packagePathResolver.produce(packagePath).get();

        if (pkg instanceof UnrecognizedGolangPackage) {
            Optional<NotationDependency> resultInCache = fetchFromCache(pkg.getPath());
            if (resultInCache.isPresent()) {
                return resultInCache.get();
            }
            NotationDependency ret = doParse(notation, pkg);
            if (!(ret instanceof UnrecognizedPackageNotationDependency)) {
                unresolvedPackageResultCache.put(Paths.get(ret.getName()), ret);
            }
            return ret;
        } else {
            return doParse(notation, pkg);
        }
    }

    private Optional<NotationDependency> fetchFromCache(Path packagePath) {
        for (int i = packagePath.getNameCount(); i > 0; i--) {
            Path subpath = packagePath.subpath(0, i);
            NotationDependency result = unresolvedPackageResultCache.get(subpath);
            if (result != null) {
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    private NotationDependency doParse(Map<String, Object> notation, GolangPackage pkg) {
        if (notation.containsKey(DIR_KEY)) {
            return dirMapNotationParser.parse(notation);
        } else if (notation.containsKey(VENDOR_PATH_KEY)) {
            return vendorMapNotationParser.parse(notation);
        } else {
            return parseWithVcs(notation, pkg);
        }
    }

    private NotationDependency parseWithVcs(Map<String, Object> notation, GolangPackage pkg) {
        if (pkg instanceof VcsGolangPackage) {
            VcsGolangPackage vcsGolangPackage = (VcsGolangPackage) pkg;
            NotationDependency ret = parseVcsPackage(notation, vcsGolangPackage);
            substituteUrls(ret, vcsGolangPackage);
            return ret;
        } else if (pkg instanceof UnrecognizedGolangPackage) {
            return parseUnrecognizedPackage(notation, (UnrecognizedGolangPackage) pkg);
        } else {
            throw DependencyResolutionException.cannotParseNotation(notation);
        }
    }

    private NotationDependency parseUnrecognizedPackage(Map<String, Object> notation,
                                                        UnrecognizedGolangPackage pkg) {
        Optional<String> url = tryGetUrl(notation, pkg);
        if (url.isPresent()) {
            return parseVcsPackage(notation, adaptAsVcsPackage(notation, pkg, url.get()));
        } else {
            return UnrecognizedPackageNotationDependency.of(pkg);
        }
    }

    private VcsGolangPackage adaptAsVcsPackage(Map<String, Object> notation,
                                               UnrecognizedGolangPackage pkg,
                                               String url) {
        return VcsGolangPackage.builder()
                .withVcsType(determineVcs(notation))
                .withPath(pkg.getPath())
                .withRootPath(pkg.getPath())
                .withUrl(url)
                .withTemp(true)
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
        if (StringUtils.isBlank(vcs)) {
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
                .distinct()
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
        if (isNotBlank(declaredVcs)) {
            String actualVcs = pkg.getVcsType().getName();
            Assert.isTrue(declaredVcs.equals(actualVcs),
                    "Vcs type not match: declared is " + declaredVcs + " but actual is " + actualVcs);
        }
    }

}
