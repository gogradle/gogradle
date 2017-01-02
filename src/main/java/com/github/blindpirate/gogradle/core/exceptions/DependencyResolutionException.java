package com.github.blindpirate.gogradle.core.exceptions;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.vcs.git.GitNotationDependency;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.GradleException;

import java.io.File;
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

    public static DependencyResolutionException sourceCodeParsingFailed(ResolvedDependency module, IOException e) {
        return new DependencyResolutionException("Parsing source code of " + module.getName() + " failed.", e);
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

    public static DependencyResolutionException cannotParseGodepsDotJson(ResolvedDependency module, IOException e) {
        return new DependencyResolutionException("Cannot parse godeps.json of " + module.getName(), e);
    }

    public static DependencyResolutionException cannotFindGitCommit(GitNotationDependency gitNotationDependency) {
        return new DependencyResolutionException("Cannot find commit " + gitNotationDependency.getCommit()
                + " in repository of " + gitNotationDependency.getName() + ", did they force to delete this commit?");
    }

    public static DependencyResolutionException cannotResolveDependency(GolangDependency dependency, Exception e) {
        return new DependencyResolutionException("Cannot produce dependency to go code:" + dependency, e);
    }

    public static DependencyResolutionException cannotResolveDependency(GolangDependency dependency) {
        return cannotResolveDependency(dependency, null);
    }

    public static DependencyResolutionException directoryIsInvalid(File rootDir) {
        return new DependencyResolutionException("Directory is invalid:" + rootDir.getPath());
    }
}
