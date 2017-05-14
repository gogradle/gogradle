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
                + " does not exist in transitive dependencies of " + resolvedDependency);
    }

    public static DependencyResolutionException cannotFindGitTag(NotationDependency dependency, String tag) {
        return new DependencyResolutionException("Cannot find tag " + tag
                + " in repository of "
                + GitMercurialNotationDependency.class.cast(dependency).getName()
                + ", did they delete this tag?");
    }
}
