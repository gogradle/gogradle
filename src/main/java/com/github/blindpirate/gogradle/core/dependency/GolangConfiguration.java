package com.github.blindpirate.gogradle.core.dependency;

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
    @Override
    public ResolutionStrategy getResolutionStrategy() {
        return null;
    }

    @Override
    public Configuration resolutionStrategy(
            @DelegatesTo(value = ResolutionStrategy.class, strategy = 1)
                    Closure closure) {
        return null;
    }

    @Override
    public Configuration resolutionStrategy(Action<? super ResolutionStrategy> action) {
        return null;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public Configuration setVisible(boolean visible) {
        return null;
    }

    @Override
    public Set<Configuration> getExtendsFrom() {
        return null;
    }

    @Override
    public Configuration setExtendsFrom(Iterable<Configuration> superConfigs) {
        return null;
    }

    @Override
    public Configuration extendsFrom(Configuration... superConfigs) {
        return null;
    }

    @Override
    public boolean isTransitive() {
        return false;
    }

    @Override
    public Configuration setTransitive(boolean t) {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Configuration setDescription(String description) {
        return null;
    }

    @Override
    public Set<Configuration> getHierarchy() {
        return null;
    }

    @Override
    public Set<File> resolve() {
        return null;
    }

    @Override
    public Set<File> files(Closure dependencySpecClosure) {
        return null;
    }

    @Override
    public Set<File> files(Spec<? super Dependency> dependencySpec) {
        return null;
    }

    @Override
    public Set<File> files(Dependency... dependencies) {
        return null;
    }

    @Override
    public FileCollection fileCollection(Spec<? super Dependency> dependencySpec) {
        return null;
    }

    @Override
    public FileCollection fileCollection(Closure dependencySpecClosure) {
        return null;
    }

    @Override
    public FileCollection fileCollection(Dependency... dependencies) {
        return null;
    }

    @Override
    public ResolvedConfiguration getResolvedConfiguration() {
        return null;
    }

    @Override
    public String getUploadTaskName() {
        return null;
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return null;
    }

    @Override
    public TaskDependency getTaskDependencyFromProjectDependency(boolean useDependedOn,
                                                                 String taskName) {
        return null;
    }

    @Override
    public DependencySet getDependencies() {
        return null;
    }

    @Override
    public DependencySet getAllDependencies() {
        return null;
    }

    @Override
    public PublishArtifactSet getArtifacts() {
        return null;
    }

    @Override
    public PublishArtifactSet getAllArtifacts() {
        return null;
    }

    @Override
    public Set<ExcludeRule> getExcludeRules() {
        return null;
    }

    @Override
    public Configuration exclude(Map<String, String> excludeProperties) {
        return null;
    }

    @Override
    public Configuration defaultDependencies(Action<? super DependencySet> action) {
        return null;
    }

    @Override
    public Set<Configuration> getAll() {
        return null;
    }

    @Override
    public ResolvableDependencies getIncoming() {
        return null;
    }

    @Override
    public Configuration copy() {
        return null;
    }

    @Override
    public Configuration copyRecursive() {
        return null;
    }

    @Override
    public Configuration copy(Spec<? super Dependency> dependencySpec) {
        return null;
    }

    @Override
    public Configuration copyRecursive(Spec<? super Dependency> dependencySpec) {
        return null;
    }

    @Override
    public Configuration copy(Closure dependencySpec) {
        return null;
    }

    @Override
    public Configuration copyRecursive(Closure dependencySpec) {
        return null;
    }

    @Override
    public Configuration attribute(String key, String value) {
        return null;
    }

    @Override
    public Configuration attributes(Map<String, String> attributes) {
        return null;
    }

    @Override
    public Map<String, String> getAttributes() {
        return null;
    }

    @Override
    public boolean hasAttributes() {
        return false;
    }

    @Override
    public File getSingleFile() throws IllegalStateException {
        return null;
    }

    @Override
    public Set<File> getFiles() {
        return null;
    }

    @Override
    public boolean contains(File file) {
        return false;
    }

    @Override
    public String getAsPath() {
        return null;
    }

    @Override
    public FileCollection plus(FileCollection collection) {
        return null;
    }

    @Override
    public FileCollection minus(FileCollection collection) {
        return null;
    }

    @Override
    public FileCollection filter(Closure filterClosure) {
        return null;
    }

    @Override
    public FileCollection filter(Spec<? super File> filterSpec) {
        return null;
    }

    @Override
    public Object asType(Class<?> type) throws UnsupportedOperationException {
        return null;
    }

    @Override
    public FileCollection add(FileCollection collection) throws UnsupportedOperationException {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public FileCollection stopExecutionIfEmpty() throws StopExecutionException {
        return null;
    }

    @Override
    public FileTree getAsFileTree() {
        return null;
    }

    @Override
    public void addToAntBuilder(Object builder, String nodeName, AntType type) {

    }

    @Override
    public Object addToAntBuilder(Object builder, String nodeName) {
        return null;
    }

    @Override
    public Iterator<File> iterator() {
        return null;
    }
}
