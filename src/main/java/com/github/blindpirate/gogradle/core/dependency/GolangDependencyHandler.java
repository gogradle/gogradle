package com.github.blindpirate.gogradle.core.dependency;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.ComponentMetadataHandler;
import org.gradle.api.artifacts.dsl.ComponentModuleMetadataHandler;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.query.ArtifactResolutionQuery;

import java.util.Map;

public class GolangDependencyHandler implements DependencyHandler {
    @Override
    public Dependency add(String configurationName, Object dependencyNotation) {
        return null;
    }

    @Override
    public Dependency add(String configurationName, Object dependencyNotation, Closure configureClosure) {
        return null;
    }

    @Override
    public Dependency create(Object dependencyNotation) {
        return null;
    }

    @Override
    public Dependency create(Object dependencyNotation, Closure configureClosure) {
        return null;
    }

    @Override
    public Dependency module(Object notation) {
        return null;
    }

    @Override
    public Dependency module(Object notation, Closure configureClosure) {
        return null;
    }

    @Override
    public Dependency project(Map<String, ?> notation) {
        return null;
    }

    @Override
    public Dependency gradleApi() {
        return null;
    }

    @Override
    public Dependency gradleTestKit() {
        return null;
    }

    @Override
    public Dependency localGroovy() {
        return null;
    }

    @Override
    public ComponentMetadataHandler getComponents() {
        return null;
    }

    @Override
    public void components(Action<? super ComponentMetadataHandler> configureAction) {

    }

    @Override
    public ComponentModuleMetadataHandler getModules() {
        return null;
    }

    @Override
    public void modules(Action<? super ComponentModuleMetadataHandler> configureAction) {

    }

    @Override
    public ArtifactResolutionQuery createArtifactResolutionQuery() {
        return null;
    }
}
