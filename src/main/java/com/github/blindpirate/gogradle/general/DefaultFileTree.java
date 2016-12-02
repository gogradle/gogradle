package com.github.blindpirate.gogradle.general;

import groovy.lang.Closure;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

public class DefaultFileTree implements ConfigurableFileTree {
    @Override
    public ConfigurableFileTree from(Object dir) {
        return null;
    }

    @Override
    public File getDir() {
        return null;
    }

    @Override
    public PatternSet getPatterns() {
        return null;
    }

    @Override
    public ConfigurableFileTree setDir(Object dir) {
        return null;
    }

    @Override
    public Set<Object> getBuiltBy() {
        return null;
    }

    @Override
    public ConfigurableFileTree setBuiltBy(Iterable<?> tasks) {
        return null;
    }

    @Override
    public ConfigurableFileTree builtBy(Object... tasks) {
        return null;
    }

    @Override
    public FileTree matching(Closure filterConfigClosure) {
        return null;
    }

    @Override
    public FileTree matching(PatternFilterable patterns) {
        return null;
    }

    @Override
    public FileTree visit(FileVisitor visitor) {
        return null;
    }

    @Override
    public FileTree visit(Closure visitor) {
        return null;
    }

    @Override
    public FileTree plus(FileTree fileTree) {
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
    public Iterator<File> iterator() {
        return null;
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return null;
    }

    @Override
    public Set<String> getIncludes() {
        return null;
    }

    @Override
    public Set<String> getExcludes() {
        return null;
    }

    @Override
    public PatternFilterable setIncludes(Iterable<String> includes) {
        return null;
    }

    @Override
    public PatternFilterable setExcludes(Iterable<String> excludes) {
        return null;
    }

    @Override
    public PatternFilterable include(String... includes) {
        return null;
    }

    @Override
    public PatternFilterable include(Iterable<String> includes) {
        return null;
    }

    @Override
    public PatternFilterable include(Spec<FileTreeElement> includeSpec) {
        return null;
    }

    @Override
    public PatternFilterable include(Closure includeSpec) {
        return null;
    }

    @Override
    public PatternFilterable exclude(String... excludes) {
        return null;
    }

    @Override
    public PatternFilterable exclude(Iterable<String> excludes) {
        return null;
    }

    @Override
    public PatternFilterable exclude(Spec<FileTreeElement> excludeSpec) {
        return null;
    }

    @Override
    public PatternFilterable exclude(Closure excludeSpec) {
        return null;
    }
}
