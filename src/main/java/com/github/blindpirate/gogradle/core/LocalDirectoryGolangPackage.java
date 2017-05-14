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

import com.github.blindpirate.gogradle.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

public class LocalDirectoryGolangPackage extends ResolvableGolangPackage {

    private String dir;

    private LocalDirectoryGolangPackage(Path rootPath, Path path, String dir) {
        super(rootPath, path);
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

    @Override
    protected Optional<GolangPackage> longerPath(Path packagePath) {
        return Optional.of(of(getRootPath(), packagePath, dir));
    }

    @Override
    protected Optional<GolangPackage> shorterPath(Path packagePath) {
        if (StringUtils.toUnixString(packagePath).length() < getRootPathString().length()) {
            return Optional.of(IncompleteGolangPackage.of(packagePath));
        } else {
            return Optional.of(of(getRootPath(), packagePath, dir));
        }
    }

    public static LocalDirectoryGolangPackage of(Path rootPath, Path path, String dir) {
        return new LocalDirectoryGolangPackage(rootPath, path, dir);
    }

    public static LocalDirectoryGolangPackage of(String rootPath, String path, String dir) {
        return new LocalDirectoryGolangPackage(Paths.get(rootPath), Paths.get(path), dir);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        LocalDirectoryGolangPackage that = (LocalDirectoryGolangPackage) o;
        return Objects.equals(dir, that.dir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dir);
    }
}
