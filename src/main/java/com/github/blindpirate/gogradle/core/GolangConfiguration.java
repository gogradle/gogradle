package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.core.dependency.DefaultDependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.PublishArtifactSet;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.internal.collections.CollectionFilter;
import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.api.tasks.TaskDependency;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GolangConfiguration implements Configuration {

    public static final String BUILD = "build";
    public static final String TEST = "test";

    private final String name;
    private final GolangDependencySet dependencies = new GolangDependencySet();
    private final DependencyRegistry dependencyRegistry = new DefaultDependencyRegistry();

    public GolangConfiguration(String name) {
        this.name = name;
    }

    public DependencyRegistry getDependencyRegistry() {
        return dependencyRegistry;
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
    public FileCollection add(FileCollection collection) {
        throw new UnsupportedOperationException("Unsupported method add is invoked!");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Unsupported method isEmpty is invoked!");
    }

    @Override
    public Iterator<File> iterator() {
        throw new UnsupportedOperationException("Unsupported method iterator is invoked!");
    }

    @Override
    public ResolutionStrategy getResolutionStrategy() {
        throw new UnsupportedOperationException("Unsupported method getResolutionStrategy is invoked!");
    }

    @Override
    public Configuration resolutionStrategy(
            @DelegatesTo(value = ResolutionStrategy.class, strategy = 1)
                    Closure closure) {
        throw new UnsupportedOperationException("Unsupported method resolutionStrategy is invoked!");
    }

    @Override
    public Configuration resolutionStrategy(Action<? super ResolutionStrategy> action) {
        throw new UnsupportedOperationException("Unsupported method resolutionStrategy is invoked!");
    }

    @Override
    public State getState() {
        throw new UnsupportedOperationException("Unsupported method getState is invoked!");
    }

    @Override
    public boolean isVisible() {
        throw new UnsupportedOperationException("Unsupported method isVisible is invoked!");
    }

    @Override
    public Configuration setVisible(boolean visible) {
        throw new UnsupportedOperationException("Unsupported method setVisible is invoked!");
    }

    @Override
    public Set<Configuration> getExtendsFrom() {
        throw new UnsupportedOperationException("Unsupported method getExtendsFrom is invoked!");
    }

    @Override
    public Configuration setExtendsFrom(Iterable<Configuration> superConfigs) {
        throw new UnsupportedOperationException("Unsupported method setExtendsFrom is invoked!");
    }

    @Override
    public Configuration extendsFrom(Configuration... superConfigs) {
        throw new UnsupportedOperationException("Unsupported method extendsFrom is invoked!");
    }

    @Override
    public boolean isTransitive() {
        throw new UnsupportedOperationException("Unsupported method isTransitive is invoked!");
    }

    @Override
    public Configuration setTransitive(boolean t) {
        throw new UnsupportedOperationException("Unsupported method setTransitive is invoked!");
    }

    @Override
    public String getDescription() {
        return getName() + " configuration";
    }

    @Override
    public Configuration setDescription(String description) {
        throw new UnsupportedOperationException("Unsupported method setDescription is invoked!");
    }

    @Override
    public Set<Configuration> getHierarchy() {
        throw new UnsupportedOperationException("Unsupported method getHierarchy is invoked!");
    }

    @Override
    public Set<File> resolve() {
        throw new UnsupportedOperationException("Unsupported method resolve is invoked!");
    }

    @Override
    public Set<File> files(Closure dependencySpecClosure) {
        throw new UnsupportedOperationException("Unsupported method files is invoked!");
    }

    @Override
    public Set<File> files(Spec<? super Dependency> dependencySpec) {
        throw new UnsupportedOperationException("Unsupported method files is invoked!");
    }

    @Override
    public Set<File> files(Dependency... dependencies) {
        throw new UnsupportedOperationException("Unsupported method files is invoked!");
    }

    @Override
    public FileCollection fileCollection(Spec<? super Dependency> dependencySpec) {
        throw new UnsupportedOperationException("Unsupported method fileCollection is invoked!");
    }

    @Override
    public FileCollection fileCollection(Closure dependencySpecClosure) {
        throw new UnsupportedOperationException("Unsupported method fileCollection is invoked!");
    }

    @Override
    public FileCollection fileCollection(Dependency... dependencies) {
        throw new UnsupportedOperationException("Unsupported method fileCollection is invoked!");
    }

    @Override
    public ResolvedConfiguration getResolvedConfiguration() {
        throw new UnsupportedOperationException("Unsupported method getResolvedConfiguration is invoked!");
    }

    @Override
    public String getUploadTaskName() {
        throw new UnsupportedOperationException("Unsupported method getUploadTaskName is invoked!");
    }

    @Override
    public TaskDependency getBuildDependencies() {
        throw new UnsupportedOperationException("Unsupported method getBuildDependencies is invoked!");
    }

    @Override
    public TaskDependency getTaskDependencyFromProjectDependency(boolean useDependedOn,
                                                                 String taskName) {
        throw new UnsupportedOperationException("Unsupported method getTaskDependencyFromProjectDependency "
                + "is invoked!");
    }

    @Override
    public DependencySet getAllDependencies() {
        // hacking for IDEA
        return new GolangDependencySet().toDependencySet();
    }

    @Override
    public PublishArtifactSet getArtifacts() {
        // hacking for IDEA
        return new EmptyPublishArtifactSet();
    }

    @Override
    public PublishArtifactSet getAllArtifacts() {
        // hacking for IDEA
        return new EmptyPublishArtifactSet();
    }

    @Override
    public Set<ExcludeRule> getExcludeRules() {
        throw new UnsupportedOperationException("Unsupported method getExcludeRules is invoked!");
    }

    @Override
    public Configuration exclude(Map<String, String> excludeProperties) {
        throw new UnsupportedOperationException("Unsupported method exclude is invoked!");
    }

    @Override
    public Configuration defaultDependencies(Action<? super DependencySet> action) {
        throw new UnsupportedOperationException("Unsupported method defaultDependencies is invoked!");
    }

    @Override
    public Set<Configuration> getAll() {
        throw new UnsupportedOperationException("Unsupported method getAll is invoked!");
    }

    @Override
    public ResolvableDependencies getIncoming() {
        throw new UnsupportedOperationException("Unsupported method getIncoming is invoked!");
    }

    @Override
    public ConfigurationPublications getOutgoing() {
        throw new UnsupportedOperationException("Unsupported method getOutgoing is invoked!");
    }

    @Override
    public void outgoing(Action<? super ConfigurationPublications> action) {
        throw new UnsupportedOperationException("Unsupported method outgoing is invoked!");
    }

    @Override
    public Configuration copy() {
        throw new UnsupportedOperationException("Unsupported method copy is invoked!");
    }

    @Override
    public Configuration copyRecursive() {
        throw new UnsupportedOperationException("Unsupported method copyRecursive is invoked!");
    }

    @Override
    public Configuration copy(Spec<? super Dependency> dependencySpec) {
        throw new UnsupportedOperationException("Unsupported method copy is invoked!");
    }

    @Override
    public Configuration copyRecursive(Spec<? super Dependency> dependencySpec) {
        throw new UnsupportedOperationException("Unsupported method copyRecursive is invoked!");
    }

    @Override
    public Configuration copy(Closure dependencySpec) {
        throw new UnsupportedOperationException("Unsupported method copy is invoked!");
    }

    @Override
    public Configuration copyRecursive(Closure dependencySpec) {
        throw new UnsupportedOperationException("Unsupported method copyRecursive is invoked!");
    }

    @Override
    public void setCanBeConsumed(boolean b) {

    }

    @Override
    public boolean isCanBeConsumed() {
        return false;
    }

    @Override
    public void setCanBeResolved(boolean b) {

    }

    @Override
    public boolean isCanBeResolved() {
        return false;
    }

    @Override
    public File getSingleFile() throws IllegalStateException {
        throw new UnsupportedOperationException("Unsupported method getSingleFile is invoked!");
    }

    @Override
    public Set<File> getFiles() {
        throw new UnsupportedOperationException("Unsupported method getFiles is invoked!");
    }

    @Override
    public boolean contains(File file) {
        throw new UnsupportedOperationException("Unsupported method contains is invoked!");
    }

    @Override
    public String getAsPath() {
        throw new UnsupportedOperationException("Unsupported method getAsPath is invoked!");
    }

    @Override
    public FileCollection plus(FileCollection collection) {
        throw new UnsupportedOperationException("Unsupported method plus is invoked!");
    }

    @Override
    public FileCollection minus(FileCollection collection) {
        throw new UnsupportedOperationException("Unsupported method minus is invoked!");
    }

    @Override
    public FileCollection filter(Closure filterClosure) {
        throw new UnsupportedOperationException("Unsupported method filter is invoked!");
    }

    @Override
    public FileCollection filter(Spec<? super File> filterSpec) {
        throw new UnsupportedOperationException("Unsupported method filter is invoked!");
    }

    @Override
    public Object asType(Class<?> type) {
        throw new UnsupportedOperationException("Unsupported method asType is invoked!");
    }

    @Override
    public FileCollection stopExecutionIfEmpty() throws StopExecutionException {
        throw new UnsupportedOperationException("Unsupported method stopExecutionIfEmpty is invoked!");
    }

    @Override
    public FileTree getAsFileTree() {
        throw new UnsupportedOperationException("Unsupported method getAsFileTree is invoked!");
    }

    @Override
    public void addToAntBuilder(Object builder, String nodeName, AntType type) {
        throw new UnsupportedOperationException("Unsupported method addToAntBuilder is invoked!");
    }

    @Override
    public Object addToAntBuilder(Object builder, String nodeName) {
        throw new UnsupportedOperationException("Unsupported method addToAntBuilder is invoked!");
    }

    @Override
    public Configuration attributes(Action<? super AttributeContainer> action) {
        throw new UnsupportedOperationException("Unsupported method attributes is invoked!");
    }

    @Override
    public AttributeContainer getAttributes() {
        throw new UnsupportedOperationException("Unsupported method getAttributes is invoked!");
    }

    private static class EmptyPublishArtifactSet extends DefaultDomainObjectSet<PublishArtifact>
            implements PublishArtifactSet {
        protected EmptyPublishArtifactSet() {
            super(new DefaultDomainObjectSet<>(PublishArtifact.class),
                    new CollectionFilter<>(PublishArtifact.class));
        }

        @Override
        public FileCollection getFiles() {
            return new SimpleFileCollection();
        }

        @Override
        public TaskDependency getBuildDependencies() {
            return null;
        }
    }

}
