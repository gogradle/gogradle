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

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.common.GoSourceCodeFilter;
import com.github.blindpirate.gogradle.core.GolangConfiguration;
import com.github.blindpirate.gogradle.core.GolangConfigurationManager;
import com.github.blindpirate.gogradle.core.cache.ProjectCacheManager;
import com.github.blindpirate.gogradle.core.dependency.GogradleRootProject;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolveContext;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.GogradleRootProduceStrategy;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeFactory;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.tasks.TaskExecutionOutcome;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME;
import static com.github.blindpirate.gogradle.util.IOUtils.filterFilesRecursively;

public abstract class ResolveTask extends AbstractGolangTask {
    @Inject
    private GolangPluginSetting setting;

    @Inject
    private DependencyTreeFactory dependencyTreeFactory;

    @Inject
    private GogradleRootProduceStrategy strategy;

    @Inject
    private GolangConfigurationManager configurationManager;

    @Inject
    private DependencyVisitor visitor;

    @Inject
    private ProjectCacheManager projectCacheManager;

    @Inject
    private GogradleRootProject gogradleRootProject;

    private DependencyTreeNode dependencyTree;

    public ResolveTask() {
        dependsOn(PREPARE_TASK_NAME);
    }

    // INPUT 1: dependencies declared in build.gradle
    @Input
    public HashSet<GolangDependency> getDependencies() {
        GolangConfiguration configuration = configurationManager.getByName(getConfigurationName());
        // elements in GolangDependency are identified by name, here we want to identify them by equals
        return new HashSet<>(configuration.getDependencies());
    }

    // INPUT 2: gogradle.lock
    @InputFiles
    public File getExternalLockfiles() throws IOException {
        return new File(getProject().getRootDir(), "gogradle.lock");
    }

    // INPUT 3: all go files in specific configuration
    @InputFiles
    public Collection<File> getGoSourceFiles() {
        GoSourceCodeFilter filter = GoSourceCodeFilter.FILTERS.get(getConfigurationName());
        return filterFilesRecursively(getProject().getRootDir(), filter);
    }

    // INPUT 4: build tags
    @Input
    public List<String> getBuildTags() {
        return setting.getBuildTags();
    }

    // INPUT 5: build mode
    @Input
    public String getBuildMode() {
        return setting.getBuildMode().toString();
    }

    // INPUT 6: --refresh-dependencies
    @Input
    public String getRefreshDependenciesFlag() {
        if (GogradleGlobal.isRefreshDependencies()) {
            return UUID.randomUUID().toString();
        } else {
            return "";
        }
    }

    // INPUT 7: local dependencies
    @InputFiles
    public List<File> getLocalDirDependencies() {
        return configurationManager.getByName(getConfigurationName())
                .getDependencies().stream()
                .filter(dependency -> dependency instanceof LocalDirectoryDependency)
                .map(dependency -> (LocalDirectoryDependency) dependency)
                .map(LocalDirectoryDependency::getRootDir)
                .collect(Collectors.toList());
    }

    @OutputFile
    public File getSerializationFile() {
        return new File(getProject().getRootDir(), ".gogradle/cache/" + getConfigurationName() + ".bin");
    }

    @TaskAction
    public void resolve() {
        projectCacheManager.loadPersistenceCache();
        try {
            resolveDependencies();
            writeDependencyTreeToSerializationFile();
        } finally {
            projectCacheManager.savePersistenceCache();
        }
    }

    private void writeDependencyTreeToSerializationFile() {
        IOUtils.serialize(dependencyTree, getSerializationFile());
    }


    private void resolveDependencies() {
        GolangConfiguration configuration = configurationManager.getByName(getConfigurationName());
        ResolveContext rootContext = ResolveContext.root(configuration);

        gogradleRootProject.setDependencies(produceFirstLevelDependencies());

        dependencyTree = dependencyTreeFactory.getTree(rootContext, gogradleRootProject);
    }

    protected GolangDependencySet produceFirstLevelDependencies() {
        return strategy.produce(gogradleRootProject, getProject().getRootDir(), visitor, getConfigurationName());
    }

    public DependencyTreeNode getDependencyTree() {
        if (dependencyTree == null && taskUpToDate()) {
            readFromSerializationFile();
        }
        return dependencyTree;
    }

    private boolean taskUpToDate() {
        return TaskInternal.class.cast(this).getState().getOutcome() == TaskExecutionOutcome.UP_TO_DATE;
    }

    public GolangDependencySet getFlatDependencies() {
        DependencyTreeNode tree = getDependencyTree();
        return tree == null ? GolangDependencySet.empty() : tree.flatten();
    }

    private void readFromSerializationFile() {
        File serializationFile = getSerializationFile();
        Assert.isTrue(serializationFile.exists());
        dependencyTree = (DependencyTreeNode) IOUtils.deserialize(serializationFile);
    }

    public abstract String getConfigurationName();

}
