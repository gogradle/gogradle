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
import com.github.blindpirate.gogradle.core.pack.GloballyIgnoredPackages;
import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.task.GolangTaskContainer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;

/**
 * The entry of Gogradle plugin.
 */
public class GolangPlugin implements Plugin<Project> {
    public static final String GOGRADLE_INJECTOR = "GOGRADLE_INJECTOR";
    private static final Logger LOGGER = Logging.getLogger(GolangPlugin.class);
    private Injector injector;
    private GolangPluginSetting settings;
    private GolangTaskContainer golangTaskContainer;
    private GolangRepositoryHandler golangRepositoryHandler;
    private Project project;

    @Override
    public void apply(Project project) {
        init(project);
        configureGogradleGlobal();
        customizeProjectInternalServices(project);
        configureSettings(project);
        configureConfigurations(project);
        createCoreTasks(project);
    }

    private void init(Project project) {
        this.project = project;
        injector = initGuice();
        project.getExtensions().add(GOGRADLE_INJECTOR, this.injector);
        GogradleGlobal.INSTANCE.setCurrentProject(project);
        settings = injector.getInstance(GolangPluginSetting.class);
        settings.setVendorTargetDir(project.getProjectDir().getAbsolutePath() + "/vendor");
        golangTaskContainer = injector.getInstance(GolangTaskContainer.class).init(project, injector);
        golangRepositoryHandler = injector.getInstance(GolangRepositoryHandler.class);
        Arrays.asList(BuildMode.values()).forEach(mode -> {
            project.getExtensions().add(BuildMode.class, mode.toString(), mode);
            project.getExtensions().add(BuildMode.class, mode.getAbbr(), mode);
        });
        Arrays.asList(TimeUnit.values()).forEach(timeUnit -> {
            final String name = timeUnit.name();
            project.getExtensions().add(TimeUnit.class, name, timeUnit);
            project.getExtensions().add(TimeUnit.class, name.substring(0, name.length() - 1), timeUnit);
        });
    }

    private Injector initGuice() {
        return Guice.createInjector(new GogradleModule(project));
    }

    private void configureGogradleGlobal() {
        GogradleGlobal.INSTANCE.setCurrentProject(project);
    }

    private void createCoreTasks(Project project) {
        golangTaskContainer.createCoreTasks();
        project.afterEvaluate(this::afterEvaluate);
    }

    private void afterEvaluate(Project project) {
        this.golangTaskContainer.forEach(task -> {
            if (task instanceof AbstractGolangTask) {
                AbstractGolangTask.class.cast(task).afterEvaluate();
            }
        });
        registerGloballyIgnoredPackages();
    }

    private void registerGloballyIgnoredPackages() {
        if (!settings.isUserHasCustomizedIgnoredPackages()) {
            settings.setIgnoredPackages(GloballyIgnoredPackages.PKGS);
        }

        if (!settings.getIgnoredPackages().isEmpty()) {
            LOGGER.info("Ignore packages globally: {}", String.join(",", settings.getIgnoredPackages()));
            settings.getIgnoredPackages().forEach(golangRepositoryHandler::addEmptyRepo);
        }
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
