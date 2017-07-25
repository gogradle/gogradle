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

import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME;

/**
 * Prints the dependencies tree.
 */
public class DependenciesTask extends AbstractGolangTask {
    private static final Logger LOGGER = Logging.getLogger(DependenciesTask.class);

    public DependenciesTask() {
        setDescription("Displays all dependencies.");
        dependsOn(RESOLVE_BUILD_DEPENDENCIES_TASK_NAME,
                RESOLVE_TEST_DEPENDENCIES_TASK_NAME);
    }

    @TaskAction
    public void displayDependencies() {
        DependencyTreeNode buildTree = getTask(ResolveBuildDependencies.class).getDependencyTree();
        DependencyTreeNode testTree = getTask(ResolveTestDependenciesDependencies.class).getDependencyTree();
        display(BUILD, buildTree);
        display(TEST, testTree);
    }

    private void display(String configuration, DependencyTreeNode tree) {
        LOGGER.quiet(configuration + ":");
        LOGGER.quiet(tree.output());
    }
}
