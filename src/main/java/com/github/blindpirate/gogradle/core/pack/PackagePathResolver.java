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

package com.github.blindpirate.gogradle.core.pack;


import com.github.blindpirate.gogradle.common.Factory;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.ResolvableGolangPackage;

import java.util.Optional;

public interface PackagePathResolver extends Factory<String, GolangPackage> {
    String HTTP = "http://";
    String HTTPS = "https://";

    @Override
    Optional<GolangPackage> produce(String packagePath);

    default String rootPath(String packagePath) {
        GolangPackage pkg = produce(packagePath).get();
        return ResolvableGolangPackage.class.cast(pkg).getRootPathString();
    }
}
