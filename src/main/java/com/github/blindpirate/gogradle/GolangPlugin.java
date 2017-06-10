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

package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.core.GolangDependencyHandler;
import com.github.blindpirate.gogradle.core.mode.BuildMode;
import com.github.blindpirate.gogradle.ide.IdeaIntegration;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.TaskContainer;

import java.util.Arrays;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.BUILD_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.GOFMT_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.GOVET_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.TASKS;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.TEST_TASK_NAME;
import static java.util.Arrays.asList;

/**
 * The entry of Gogradle plugin.
 */
public class GolangPlugin implements Plugin<Project> {
    private Injector injector;
    private GolangPluginSetting settings;
    private GolangTaskContainer golangTaskContainer;
    private Project project;
    private Action<? super Task> dependencyInjectionAction = new Action<Task>() {
        @Override
        public void execute(Task task) {
            injector.injectMembers(task);
        }
    };

    // This is invoked by gradle, not our Guice.
    private Injector initGuice() {
        return Guice.createInjector(new GogradleModule(project));
    }

    @Override
    public void apply(Project project) {
        init(project);
        configureGlobalInjector();
        customizeProjectInternalServices(project);
        configureSettings(project);
        configureConfigurations(project);
        configureTasks(project);
        hackIdeaPlugin();
    }

    private void hackIdeaPlugin() {
        project.getPlugins().withId("idea", plugin -> injector.getInstance(IdeaIntegration.class).hack());
    }

    private void init(Project project) {
        this.project = project;
        this.injector = initGuice();
        this.settings = injector.getInstance(GolangPluginSetting.class);
        this.golangTaskContainer = injector.getInstance(GolangTaskContainer.class);
        Arrays.asList(BuildMode.values()).forEach(mode -> {
            project.getExtensions().add(mode.toString(), mode);
            project.getExtensions().add(mode.getAbbr(), mode);
        });
    }

    private void configureGlobalInjector() {
        GogradleGlobal.INSTANCE.setInjector(injector);
    }

    private void configureTasks(Project project) {
        TaskContainer taskContainer = project.getTasks();
        TASKS.entrySet().forEach(entry -> {
            Task task = taskContainer.create(entry.getKey(), entry.getValue(), dependencyInjectionAction);
            golangTaskContainer.put((Class) entry.getValue(), task);
        });

        project.afterEvaluate(this::afterEvaluate);
    }

    private void afterEvaluate(Project p) {
        asList(BUILD_TASK_NAME, TEST_TASK_NAME, GOFMT_TASK_NAME, GOVET_TASK_NAME).forEach(
                task -> Go.class.cast(p.getTasks().getByName(task)).addDefaultActionIfNoCustomActions());
    }

    private void customizeProjectInternalServices(Project project) {
        bindCustomizedService(project.getRepositories(), injector.getInstance(GolangRepositoryHandler.class));
        bindCustomizedService(project.getDependencies(), injector.getInstance(GolangDependencyHandler.class));
    }

    private void bindCustomizedService(Object target, Object customizedService) {
        ExtensionAware.class.cast(target).getExtensions().add("golang", customizedService);
    }

    private void configureConfigurations(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();
        Arrays.asList(BUILD, TEST).forEach(configurations::create);
    }

    private void configureSettings(Project project) {
        project.getExtensions().add("golang", settings);
    }
}
