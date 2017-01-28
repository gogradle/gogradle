package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.PublishArtifactSet;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.api.tasks.TaskDependency;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GolangConfiguration implements Configuration {

    private final String name;
    private final GolangDependencySet dependencies = new GolangDependencySet();

    public GolangConfiguration(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DependencySet getDependencies() {
        return dependencies.toDependencySet();
    }

    @Override
    public FileCollection add(FileCollection collection) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<File> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration resolutionStrategy(
            @DelegatesTo(value = ResolutionStrategy.class, strategy = 1)
                    Closure closure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration resolutionStrategy(Action<? super ResolutionStrategy> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public State getState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVisible() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration setVisible(boolean visible) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Configuration> getExtendsFrom() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration setExtendsFrom(Iterable<Configuration> superConfigs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration extendsFrom(Configuration... superConfigs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTransitive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration setTransitive(boolean t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration setDescription(String description) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Configuration> getHierarchy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<File> resolve() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<File> files(Closure dependencySpecClosure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<File> files(Spec<? super Dependency> dependencySpec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<File> files(Dependency... dependencies) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileCollection fileCollection(Spec<? super Dependency> dependencySpec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileCollection fileCollection(Closure dependencySpecClosure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileCollection fileCollection(Dependency... dependencies) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResolvedConfiguration getResolvedConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUploadTaskName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskDependency getBuildDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskDependency getTaskDependencyFromProjectDependency(boolean useDependedOn,
                                                                 String taskName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DependencySet getAllDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PublishArtifactSet getArtifacts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PublishArtifactSet getAllArtifacts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ExcludeRule> getExcludeRules() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration exclude(Map<String, String> excludeProperties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration defaultDependencies(Action<? super DependencySet> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Configuration> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResolvableDependencies getIncoming() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration copyRecursive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration copy(Spec<? super Dependency> dependencySpec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration copyRecursive(Spec<? super Dependency> dependencySpec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration copy(Closure dependencySpec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration copyRecursive(Closure dependencySpec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration attribute(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration attributes(Map<String, String> attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getSingleFile() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<File> getFiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(File file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAsPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileCollection plus(FileCollection collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileCollection minus(FileCollection collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileCollection filter(Closure filterClosure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileCollection filter(Spec<? super File> filterSpec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object asType(Class<?> type) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileCollection stopExecutionIfEmpty() throws StopExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileTree getAsFileTree() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addToAntBuilder(Object builder, String nodeName, AntType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addToAntBuilder(Object builder, String nodeName) {
        throw new UnsupportedOperationException();
    }

}
