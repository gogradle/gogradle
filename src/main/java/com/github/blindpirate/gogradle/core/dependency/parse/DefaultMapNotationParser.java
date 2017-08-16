/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage;
import com.github.blindpirate.gogradle.core.ResolvableGolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.UnrecognizedNotationDependency;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.core.pack.DefaultPackagePathResolver;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.MapUtils.getString;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.URL_KEY;
import static java.util.Collections.singletonList;

/**
 * Converts a map notation to a {@link NotationDependency}
 * which can be resolved to a {@link AbstractResolvedDependency}
 */
@Singleton
public class DefaultMapNotationParser implements MapNotationParser {
    private final DirMapNotationParser dirMapNotationParser;
    private final VendorMapNotationParser vendorMapNotationParser;
    private final DefaultPackagePathResolver packagePathResolver;

    @Inject
    public DefaultMapNotationParser(DirMapNotationParser dirMapNotationParser,
                                    VendorMapNotationParser vendorMapNotationParser,
                                    DefaultPackagePathResolver packagePathResolver) {
        this.dirMapNotationParser = dirMapNotationParser;
        this.vendorMapNotationParser = vendorMapNotationParser;
        this.packagePathResolver = packagePathResolver;
    }

    @Override
    public NotationDependency parse(Map<String, Object> notation) {
        Assert.isTrue(notation.containsKey(NAME_KEY), "Name must be specified!");

        String packagePath = getString(notation, NAME_KEY);

        GolangPackage pkg = packagePathResolver.produce(packagePath).get();

        if (pkg instanceof ResolvableGolangPackage) {
            String rootPathString = ResolvableGolangPackage.class.cast(pkg).getRootPathString();
            notation.put(NAME_KEY, rootPathString);
            notation.put(PACKAGE_KEY, ResolvableGolangPackage.class.cast(pkg).resolve(rootPathString).get());
        } else if (pkg instanceof UnrecognizedGolangPackage) {
            // https://github.com/gogradle/gogradle/issues/141
            if (notation.containsKey(DIR_KEY)) {
                pkg = LocalDirectoryGolangPackage.of(packagePath, packagePath, getString(notation, DIR_KEY));
                packagePathResolver.updateCache(packagePath, pkg);
            } else if (notation.containsKey(URL_KEY)) {
                pkg = adaptAsVcsPackage(notation, packagePath);
                packagePathResolver.updateCache(packagePath, pkg);
            }
            notation.put(PACKAGE_KEY, pkg);
        }

        if (notation.containsKey(DIR_KEY) || pkg instanceof LocalDirectoryGolangPackage) {
            return dirMapNotationParser.parse(notation);
        } else if (notation.containsKey(VENDOR_PATH_KEY)) {
            return vendorMapNotationParser.parse(notation);
        } else if (pkg instanceof VcsGolangPackage) {
            return parseVcsPackage(notation, (VcsGolangPackage) pkg);
        } else if (pkg instanceof UnrecognizedGolangPackage) {
            return UnrecognizedNotationDependency.of((UnrecognizedGolangPackage) pkg);
        } else {
            throw DependencyResolutionException.cannotParseNotation(notation);
        }
    }

    private VcsGolangPackage adaptAsVcsPackage(Map<String, Object> notation,
                                               String rootPath) {
        String url = MapUtils.getString(notation, URL_KEY);
        return VcsGolangPackage.builder()
                .withSubstitutedVcsInfo(determineVcs(notation), singletonList(url))
                .withPath(rootPath)
                .withRootPath(rootPath)
                .build();
    }

    private NotationDependency parseVcsPackage(Map<String, Object> notation, VcsGolangPackage pkg) {
        verifyVcs(notation, pkg);
        return pkg.getVcsType().getService(MapNotationParser.class).parse(notation);
    }

    private VcsType determineVcs(Map<String, Object> notation) {
        String vcs = getString(notation, VCS_KEY);
        if (vcs == null) {
            notation.put(VCS_KEY, VcsType.GIT.getName());
            return VcsType.GIT;
        } else {
            return VcsType.of(vcs).get();
        }
    }

    private void verifyVcs(Map<String, Object> notation, VcsGolangPackage pkg) {
        String declaredVcs = getString(notation, VCS_KEY);
        if (StringUtils.isNotBlank(declaredVcs)) {
            String actualVcs = pkg.getVcsType().getName();
            Assert.isTrue(declaredVcs.equals(actualVcs),
                    "Vcs type not match: declared is " + declaredVcs + " but actual is " + actualVcs);
        }
    }
}
