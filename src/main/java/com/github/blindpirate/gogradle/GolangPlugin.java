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
import com.github.blindpirate.gogradle.task.go.GoBuild;
import com.github.blindpirate.gogradle.task.go.Gofmt;
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
import java.util.concurrent.TimeUnit;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.TASKS;

/**
 * The entry of Gogradle plugin.
 */
public class GolangPlugin implements Plugin<Project> {
    public static final String GOGRADLE_INJECTOR = "GOGRADLE_INJECTOR";
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
        System.setProperty("GRADLE_MAJOR_VERSION", project.getGradle().getGradleVersion().split("\\.")[0]);
        configureGogradleGlobal();
        customizeProjectInternalServices(project);
        configureSettings(project);
        configureConfigurations(project);
        configureTasks(project);
        hackIdeaPlugin();
    }

    private void hackIdeaPlugin() {
        if (project.getRootDir().equals(project.getProjectDir())) {
            project.getPlugins().withId("idea", plugin -> injector.getInstance(IdeaIntegration.class).hack());
        }
    }

    private void init(Project project) {
        this.project = project;
        this.injector = initGuice();
        this.project.getExtensions().add(GOGRADLE_INJECTOR, this.injector);
        this.settings = injector.getInstance(GolangPluginSetting.class);
        this.golangTaskContainer = injector.getInstance(GolangTaskContainer.class);
        Arrays.asList(BuildMode.values()).forEach(mode -> {
            project.getExtensions().add(mode.toString(), mode);
            project.getExtensions().add(mode.getAbbr(), mode);
        });
        Arrays.asList(TimeUnit.values()).forEach(timeUnit -> {
            final String name = timeUnit.name();
            project.getExtensions().add(name, timeUnit);
            project.getExtensions().add(name.substring(0, name.length() - 1), timeUnit);
        });
    }

    private void configureGogradleGlobal() {
        GogradleGlobal.INSTANCE.setCurrentProject(project);
    }

    private void configureTasks(Project project) {
        TaskContainer taskContainer = project.getTasks();
        TASKS.forEach((key, value) -> {
            Task task = taskContainer.create(key, value, dependencyInjectionAction);
            golangTaskContainer.put((Class) value, task);
        });
        project.afterEvaluate(this::afterEvaluate);
    }

    private void afterEvaluate(Project project) {
        this.golangTaskContainer.get(GoBuild.class).afterEvaluate();
        this.golangTaskContainer.get(Gofmt.class).afterEvaluate();
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
