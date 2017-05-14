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
import java.util.Optional;

public class StandardGolangPackage extends GolangPackage {

    private StandardGolangPackage(Path path) {
        super(path);
    }

    public Path getRootPath() {
        return getPath();
    }

    public String getRootPathString() {
        return StringUtils.toUnixString(getPath());
    }

    @Override
    protected Optional<GolangPackage> longerPath(Path packagePath) {
        return Optional.of(of(packagePath));
    }

    @Override
    protected Optional<GolangPackage> shorterPath(Path packagePath) {
        return Optional.of(of(packagePath));
    }

    public static StandardGolangPackage of(Path path) {
        return new StandardGolangPackage(path);
    }

    public static StandardGolangPackage of(String path) {
        return new StandardGolangPackage(Paths.get(path));
    }

    @Override
    public String toString() {
        return "StandardGolangPackage{"
                + "path='" + getPathString() + '\''
                + '}';
    }
}
