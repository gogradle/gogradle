package com.github.blindpirate.gogradle.core.exceptions;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependency;
import com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency;
import org.gradle.api.GradleException;

import java.io.File;

public final class DependencyResolutionException extends GradleException {
    private DependencyResolutionException(String message) {
        super(message);
    }

    private DependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DependencyResolutionException cannotCloneRepository(String name, Throwable e) {
        return new DependencyResolutionException("Cloning repository of " + name + " failed.", e);
    }

    public static DependencyResolutionException cannotParseNotation(Object notation) {
        return new DependencyResolutionException("Cannot parse notation " + String.valueOf(notation));
    }

    public static DependencyResolutionException cannotFindGitCommit(
            GitMercurialNotationDependency gitMercurialNotationDependency) {
        return new DependencyResolutionException("Cannot find commit " + gitMercurialNotationDependency.getCommit()
                + " in repository of "
                + gitMercurialNotationDependency.getName()
                + ", did they delete this commit?");
    }

    public static DependencyResolutionException cannotResolveDependency(
            GolangDependency dependency, Exception e) {
        return new DependencyResolutionException("Cannot resolve dependency:" + dependency, e);
    }

    public static DependencyResolutionException directoryIsInvalid(File rootDir) {
        return new DependencyResolutionException("Directory is invalid:" + rootDir.getPath());
    }


    public static DependencyResolutionException vendorNotExist(VendorNotationDependency vendorNotationDependency,
                                                               ResolvedDependency resolvedDependency) {
        return new DependencyResolutionException("vendor dependency " + vendorNotationDependency.toString()
                + " does not exist in transitive dependencies in " + resolvedDependency);
    }

    public static DependencyResolutionException cannotFindGitTag(NotationDependency dependency, String tag) {
        return new DependencyResolutionException("Cannot find tag " + tag
                + " in repository of "
                + GitMercurialNotationDependency.class.cast(dependency).getName()
                + ", did they delete this tag?");
    }
}
