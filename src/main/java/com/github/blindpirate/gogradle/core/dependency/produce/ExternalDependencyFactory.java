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

package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.core.pack.StandardPackagePathResolver;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;
import static com.github.blindpirate.gogradle.util.DependencySetUtils.parseMany;
import static com.google.common.collect.ImmutableMap.of;

public abstract class ExternalDependencyFactory {
    private Map<String, Function<File, List>> adapters = of(BUILD, this::adapt, TEST, this::adaptTest);

    @Inject
    protected MapNotationParser mapNotationParser;

    @Inject
    protected PackagePathResolver packagePathResolver;

    @Inject
    private StandardPackagePathResolver standardPackagePathResolver;

    /**
     * Relative paths of the identity file.
     * For example, "Godeps/Godeps.json","glide.yaml"
     *
     * @return name of that file
     */
    public abstract String identityFileName();

    public GolangDependencySet produce(File rootDir, String configuration) {
        List<Map<String, Object>> mapNotations = extractNotations(rootDir, configuration);

        mapNotations = removeStandardPackages(mapNotations);
        return parseMany(mapNotations, mapNotationParser);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> extractNotations(File rootDir, String configuration) {
        File identityFile = identityFile(rootDir);
        return adapters.get(configuration).apply(identityFile);
    }

    public boolean canRecognize(File rootDir) {
        return identityFile(rootDir).isFile();
    }

    private List<Map<String, Object>> removeStandardPackages(List<Map<String, Object>> packages) {
        return packages.stream().filter(pkg -> {
            String path = MapUtils.getString(pkg, MapNotationParser.NAME_KEY, "");
            return !standardPackagePathResolver.isStandardPackage(Paths.get(path));
        }).collect(Collectors.toList());
    }

    /**
     * In most cases, this method won't be used because all we need is build dependencies of
     * external package. However, when gogradle is building a golang project which was originally
     * managed by an external management tool, this method will be used to analyze test dependencies of the project.
     *
     * @param identityFile the identity file
     * @return test dependency of this identity file
     */
    protected List<Map<String, Object>> adaptTest(File identityFile) {
        return Collections.emptyList();
    }

    protected abstract List<Map<String, Object>> adapt(File file);

    private File identityFile(File rootDir) {
        String identityFile = identityFileName();
        Assert.isNotBlank(identityFile, "Identity file must not be empty!");
        return new File(rootDir, identityFile);
    }
}
