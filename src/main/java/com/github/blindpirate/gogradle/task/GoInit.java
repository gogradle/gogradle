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

package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.core.GolangConfigurationManager;
import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.UnrecognizedNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.lock.DefaultLockedDependencyManager;
import com.github.blindpirate.gogradle.core.dependency.produce.DefaultDependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;
import static com.github.blindpirate.gogradle.util.StringUtils.appendNSpaces;

public class GoInit extends AbstractGolangTask {
    private static final int DEFAULT_INDENT = 4;
    private static final Logger LOGGER = Logging.getLogger(GoInit.class);

    @Inject
    @DefaultDependencyVisitor.ExternalDependencyFactories
    private List<ExternalDependencyFactory> externalDependencyFactories;

    @Inject
    private GogradleRootProject gogradleRootProject;

    @Inject
    private DependencyVisitor visitor;

    @Inject
    private GolangConfigurationManager configurationManager;

    public GoInit() {
        setDescription("Import dependencies from other dependency management tools.");
        dependsOn(GolangTaskContainer.PREPARE_TASK_NAME);
    }

    @TaskAction
    void init() {
        setGogradleGlobalContext();

        File rootDir = getProject().getProjectDir();
        if (dependenciesInBuildDotGradleExists() || new File(rootDir, "gogradle.lock").exists()) {
            LOGGER.warn("This project seems to have been initialized already, skip.");
            return;
        }

        Optional<ExternalDependencyFactory> externalDependencyFactory = findExternalDependencyFactory(rootDir);
        if (externalDependencyFactory.isPresent()) {
            initByExternalLockfiles(externalDependencyFactory.get(), rootDir);
        } else {
            initBySourceCodeScan(rootDir);
        }
    }

    private boolean dependenciesInBuildDotGradleExists() {
        // there have been some dependencies declared in build.gradle
        return configurationManager.getByName(BUILD).hasFirstLevelDependencies()
                || configurationManager.getByName(TEST).hasFirstLevelDependencies();
    }

    private void initBySourceCodeScan(File rootDir) {
        GolangDependencySet buildDeps = visitor.visitSourceCodeDependencies(gogradleRootProject, rootDir, BUILD);
        GolangDependencySet testDeps = visitor.visitSourceCodeDependencies(gogradleRootProject, rootDir, TEST);

        testDeps.removeAll(buildDeps);

        appendToBuildDotGradle(rootDir,
                convertToNotationMaps(buildDeps),
                convertToNotationMaps(testDeps));
    }

    private List<Map<String, Object>> convertToNotationMaps(GolangDependencySet set) {
        return set.stream()
                .filter(dependency -> !(dependency instanceof UnrecognizedNotationDependency))
                .map(dependency -> ImmutableMap.<String, Object>of("name", dependency.getName()))
                .collect(Collectors.toList());
    }

    private void initByExternalLockfiles(ExternalDependencyFactory factory, File rootDir) {
        List<Map<String, Object>> buildDependencies = factory.extractNotations(rootDir, BUILD);
        List<Map<String, Object>> testDependencies = factory.extractNotations(rootDir, TEST);
        appendToBuildDotGradle(rootDir, buildDependencies, testDependencies);
    }

    private void appendToBuildDotGradle(File rootDir,
                                        List<Map<String, Object>> buildDependencies,
                                        List<Map<String, Object>> testDependencies) {
        StringBuilder sb = new StringBuilder();
        sb.append("\ndependencies {\n");
        appendNSpaces(sb, DEFAULT_INDENT);
        sb.append("golang {\n");
        buildDependencies.forEach(d -> appendOneLine(sb, "build", d));
        testDependencies.forEach(d -> appendOneLine(sb, "test", d));
        appendNSpaces(sb, DEFAULT_INDENT);
        sb.append("}\n}");

        IOUtils.append(new File(rootDir, "build.gradle"), sb.toString());
    }

    private void appendOneLine(StringBuilder sb, String configuration, Map<String, Object> dependencyMap) {
        appendNSpaces(sb, 2 * DEFAULT_INDENT);
        sb.append(configuration).append("(");
        sb.append(InvokerHelper.invokeMethod(dependencyMap, "inspect", new Object[0]).toString());
        sb.append(")\n");
    }

    private Optional<ExternalDependencyFactory> findExternalDependencyFactory(File rootDir) {
        return externalDependencyFactories.stream()
                .filter(factory -> !(factory instanceof DefaultLockedDependencyManager))
                .filter(factory -> factory.canRecognize(rootDir))
                .findFirst();
    }
}
