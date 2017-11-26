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

package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DefaultNotationConverter implements NotationConverter {
    private final PackagePathResolver packagePathResolver;

    @Inject
    public DefaultNotationConverter(PackagePathResolver packagePathResolver) {
        this.packagePathResolver = packagePathResolver;
    }

    @Override
    @DebugLog
    public Map<String, Object> convert(String notation) {
        String packagePath = extractPackagePath(notation);
        GolangPackage packageInfo = packagePathResolver.produce(packagePath).get();
        if (packageInfo instanceof VcsGolangPackage) {
            VcsType vcsType = VcsGolangPackage.class.cast(packageInfo).getVcs();
            return vcsType.getNotationConverter().convert(notation);
        } else {
            return MapUtils.asMap(MapNotationParser.NAME_KEY, notation);
        }
    }

    private String extractPackagePath(String notation) {
        for (int i = 0; i < notation.length(); ++i) {
            char c = notation.charAt(i);
            if (c == '#' || c == '@') {
                return notation.substring(0, i);
            }
        }
        return notation;
    }
}
