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

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.UnrecognizedGolangPackage;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class UnrecognizedPackagePathResolver implements PackagePathResolver {
    private static final Logger LOGGER = Logging.getLogger(UnrecognizedPackagePathResolver.class);

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        LOGGER.info("Cannot recoginze {}, are you offline now?", packagePath);
        return Optional.of(UnrecognizedGolangPackage.of(packagePath));
    }
}
