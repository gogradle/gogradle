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

package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a golang package. In golang, a package is actually a normal directory.
 */
public abstract class GolangPackage implements Serializable {
    // java.io.NotSerializableException: sun.nio.fs.UnixPath
    private String pathString;

    public GolangPackage(Path path) {
        this.pathString = StringUtils.toUnixString(path);
    }

    public Path getPath() {
        return Paths.get(pathString);
    }

    public Path getRootPath() {
        return getPath();
    }

    public String getPathString() {
        return pathString;
    }

    public String getRootPathString() {
        return getPathString();
    }

    public Optional<GolangPackage> resolve(Path packagePath) {
        Path path = getPath();
        Assert.isTrue(packagePath.startsWith(path) || path.startsWith(packagePath));
        if (path.equals(packagePath)) {
            return Optional.of(this);
        } else if (path.startsWith(packagePath)) {
            return shorterPath(packagePath);
        } else {
            return longerPath(packagePath);
        }
    }

    public Optional<GolangPackage> resolve(String packagePath) {
        return resolve(Paths.get(packagePath));
    }

    protected abstract Optional<GolangPackage> longerPath(Path packagePath);

    protected abstract Optional<GolangPackage> shorterPath(Path packagePath);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GolangPackage that = (GolangPackage) o;
        return Objects.equals(pathString, that.pathString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathString);
    }
}
