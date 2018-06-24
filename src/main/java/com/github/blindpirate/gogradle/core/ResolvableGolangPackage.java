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

public abstract class ResolvableGolangPackage extends GolangPackage {
    private String rootPathString;

    protected ResolvableGolangPackage(Path rootPath, Path path) {
        super(path);
        this.rootPathString = StringUtils.toUnixString(rootPath);
    }

    @Override
    public String getRootPathString() {
        return rootPathString;
    }

    @Override
    public Path getRootPath() {
        return Paths.get(rootPathString);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        ResolvableGolangPackage that = (ResolvableGolangPackage) o;
        return Objects.equals(rootPathString, that.rootPathString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rootPathString);
    }
}
