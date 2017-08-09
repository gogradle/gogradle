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
import com.github.blindpirate.gogradle.ide.GoglandTask;
import com.github.blindpirate.gogradle.ide.IdeaTask;
import com.github.blindpirate.gogradle.ide.JetBrainsIdeTask;
import com.github.blindpirate.gogradle.ide.VscodeTask;
import com.github.blindpirate.gogradle.task.go.GoBuild;
import com.github.blindpirate.gogradle.task.go.GoCover;
import com.github.blindpirate.gogradle.task.go.GoTest;
import com.github.blindpirate.gogradle.task.go.GoVet;
import com.github.blindpirate.gogradle.task.go.Gofmt;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.Task;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
public class GolangTaskContainer {
    // prepare everything
    public static final String PREPARE_TASK_NAME = determineAlias("prepare", "goPrepare");
    // produce all dependencies by analyzing build.gradle
    public static final String RESOLVE_BUILD_DEPENDENCIES_TASK_NAME = "resolveBuildDependencies";
    public static final String RESOLVE_TEST_DEPENDENCIES_TASK_NAME = "resolveTestDependencies";
    public static final String INSTALL_DEPENDENCIES_TASK_NAME = "installDependencies";
    // show dependencies tree
    public static final String DEPENDENCIES_TASK_NAME = determineAlias("dependencies", "goDependencies");

    public static final String INIT_TASK_NAME = determineAlias("init", "goInit");
    public static final String CHECK_TASK_NAME = determineAlias("check", "goCheck");
    public static final String LOCK_TASK_NAME = determineAlias("lock", "goLock");
    public static final String BUILD_TASK_NAME = determineAlias("build", "goBuild");
    public static final String CLEAN_TASK_NAME = determineAlias("clean", "goClean");
    public static final String TEST_TASK_NAME = determineAlias("test", "goTest");
    public static final String VENDOR_TASK_NAME = determineAlias("vendor", "goVendor");
    public static final String IDEA_TASK_NAME = "goIdea";
    public static final String GOGLAND_TASK_NAME = "gogland";
    public static final String WEBSTORM_TASK_NAME = "webStorm";
    public static final String PHPSTORM_TASK_NAME = "phpStorm";
    public static final String PYCHARM_TASK_NAME = "pyCharm";
    public static final String VSCODE_TASK_NAME = "vscode";
    private static final String RUBYMINE_TASK_NAME = "rubyMine";
    private static final String CLION_TASK_NAME = "cLion";
    private static final String SHOW_GOPATH_GOROOT_TASK_NAME = "showGopathGoroot";
    public static final String COVERAGE_TASK_NAME = determineAlias("coverage", "goCover");
    public static final String GOFMT_TASK_NAME = determineAlias("fmt", "gofmt");
    public static final String GOVET_TASK_NAME = determineAlias("vet", "goVet");

    private static String determineAlias(String defaultName, String aliasName) {
        if (GogradleGlobal.isAlias()) {
            return aliasName;
        } else {
            return defaultName;
        }
    }


    public static final Map<String, Class<? extends Task>> TASKS = ImmutableMap.<String, Class<? extends Task>>builder()
            .put(PREPARE_TASK_NAME, GoPrepare.class)
            .put(RESOLVE_BUILD_DEPENDENCIES_TASK_NAME, ResolveBuildDependencies.class)
            .put(RESOLVE_TEST_DEPENDENCIES_TASK_NAME, ResolveTestDependencies.class)
            .put(DEPENDENCIES_TASK_NAME, DependenciesTask.class)
            .put(BUILD_TASK_NAME, GoBuild.class)
            .put(TEST_TASK_NAME, GoTest.class)
            .put(VENDOR_TASK_NAME, GoVendor.class)
            .put(INSTALL_DEPENDENCIES_TASK_NAME, GoInstall.class)
            .put(INIT_TASK_NAME, GoInit.class)
            .put(CLEAN_TASK_NAME, GoClean.class)
            .put(CHECK_TASK_NAME, GoCheck.class)
            .put(LOCK_TASK_NAME, GoLock.class)
            .put(IDEA_TASK_NAME, IdeaTask.class)
            .put(VSCODE_TASK_NAME, VscodeTask.class)
            .put(GOGLAND_TASK_NAME, GoglandTask.class)
            .put(WEBSTORM_TASK_NAME, JetBrainsIdeTask.class)
            .put(PHPSTORM_TASK_NAME, JetBrainsIdeTask.class)
            .put(PYCHARM_TASK_NAME, JetBrainsIdeTask.class)
            .put(RUBYMINE_TASK_NAME, JetBrainsIdeTask.class)
            .put(CLION_TASK_NAME, JetBrainsIdeTask.class)
            .put(SHOW_GOPATH_GOROOT_TASK_NAME, ShowGopathGoroot.class)
            .put(COVERAGE_TASK_NAME, GoCover.class)
            .put(GOVET_TASK_NAME, GoVet.class)
            .put(GOFMT_TASK_NAME, Gofmt.class)
            .build();

    private Map<Class<? extends Task>, Task> tasks = new HashMap<>();

    public <T extends Task> void put(Class<T> clazz, T task) {
        this.tasks.put(clazz, task);
    }

    @SuppressWarnings("unchecked")
    public <T extends Task> T get(Class<T> clazz) {
        return (T) this.tasks.get(clazz);
    }

    public void each(Consumer<Task> consumer) {
        tasks.values().forEach(consumer::accept);
    }
}
