package com.github.blindpirate.gogradle.core;

import org.gradle.api.Project;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GolangConfigurationManager {
    private final Project project;

    @Inject
    public GolangConfigurationManager(Project project) {
        this.project = project;
    }

    public GolangConfiguration getByName(String name) {
        return (GolangConfiguration) project.getConfigurations().getByName(name);
    }
}
