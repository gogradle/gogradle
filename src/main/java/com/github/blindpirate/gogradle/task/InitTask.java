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

public class InitTask extends AbstractGolangTask {
    private static final int DEFAULT_INDENT = 4;
    private static final Logger LOGGER = Logging.getLogger(InitTask.class);

    @Inject
    @DefaultDependencyVisitor.ExternalDependencyFactories
    private List<ExternalDependencyFactory> externalDependencyFactories;

    @Inject
    private DependencyVisitor visitor;

    @Inject
    private GolangConfigurationManager configurationManager;

    @Inject
    private GogradleRootProject rootProject;

    public InitTask() {
        dependsOn(GolangTaskContainer.PREPARE_TASK_NAME);
    }

    @TaskAction
    void init() {
        File rootDir = getProject().getRootDir();
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
        return !configurationManager.getByName(BUILD).getDependencies().isEmpty()
                || !configurationManager.getByName(TEST).getDependencies().isEmpty();
    }

    private void initBySourceCodeScan(File rootDir) {
        GolangDependencySet buildDependencies = visitor.visitSourceCodeDependencies(rootProject, rootDir, BUILD);
        GolangDependencySet testDependencies = visitor.visitSourceCodeDependencies(rootProject, rootDir, TEST);

        testDependencies.removeAll(buildDependencies);

        appendToBuildDotGradle(rootDir,
                convertToNotationMaps(buildDependencies),
                convertToNotationMaps(testDependencies));
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
        sb.append("dependencies {\n");
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
        sb.append(configuration).append(' ');
        sb.append(dependencyMap.entrySet().stream().map(this::mapEntryToString).collect(Collectors.joining(", ")));
        sb.append("\n");
    }

    private String mapEntryToString(Map.Entry<String, Object> entry) {
        StringBuilder sb = new StringBuilder(entry.getKey());
        sb.append(":");

        Object value = entry.getValue();
        if (value instanceof String) {
            sb.append("'").append(value).append("'");
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else {
            throw new IllegalStateException("Sorry, we should not be here: " + value);
        }
        return sb.toString();
    }

    private void appendNSpaces(StringBuilder sb, int n) {
        while (n-- > 0) {
            sb.append(' ');
        }
    }

    private Optional<ExternalDependencyFactory> findExternalDependencyFactory(File rootDir) {
        return externalDependencyFactories.stream()
                .filter(factory -> !(factory instanceof DefaultLockedDependencyManager))
                .filter(factory -> factory.canRecognize(rootDir))
                .findFirst();
    }
}
