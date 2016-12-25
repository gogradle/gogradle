package com.github.blindpirate.gogradle.core.exceptions;

import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.GitDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.GradleException;

import java.io.IOException;

public final class DependencyResolutionException extends GradleException {
    private DependencyResolutionException() {
    }

    private DependencyResolutionException(String message) {
        super(message);
    }

    private DependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    private DependencyResolutionException(Throwable e) {
        this("Dependency resolution failed, the cause is:" + e.getMessage());
    }

    public static DependencyResolutionException sourceCodeParsingFailed(GolangPackageModule module, IOException e) {
        return new DependencyResolutionException("Parsing source code of " + module.getName() + " failed.", e);
    }

    public static DependencyResolutionException cannotResolveVendor(GolangPackageModule module, IOException e) {
        return new DependencyResolutionException("Resolving vendor of " + module.getName() + " failed.", e);
    }

    public static DependencyResolutionException cannotCloneRepository(GolangDependency dependency, Throwable e) {
        return new DependencyResolutionException("Cloning repository of " + dependency.getName() + " failed.", e);
    }

    public static DependencyResolutionException cannotParseNotation(Object notation) {
        return new DependencyResolutionException("Cannot parse notation " + String.valueOf(notation));
    }

    public static DependencyResolutionException cannotResetToCommit(String commitId, GitAPIException e) {
        return new DependencyResolutionException("Cannot reset to specified commit:" + commitId, e);
    }

    public static DependencyResolutionException cannotParseGodepsDotJson(GolangPackageModule module, IOException e) {
        return new DependencyResolutionException("Cannot parse godeps.json of " + module.getName(), e);
    }

    public static DependencyResolutionException cannotFindGitCommit(GitDependency gitDependency) {
        return new DependencyResolutionException("Cannot find commit " + gitDependency.getCommit() + " in repository of "
                + gitDependency.getName() + ", did they force to delete this commit?");
    }

    public static DependencyResolutionException cannotResolveToPackage(GolangDependency dependency, Exception e) {
        return new DependencyResolutionException("Cannot resolve " + dependency.getName() + " to go code.", e);
    }
}
