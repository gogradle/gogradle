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
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.util.MapUtils;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DirMapNotationParser extends AutoConfigureMapNotationParser<LocalDirectoryDependency> {
    protected void preConfigure(Map<String, Object> notationMap) {
        GolangPackage pkg = MapUtils.getValue(notationMap, PACKAGE_KEY, GolangPackage.class);
        if (pkg instanceof LocalDirectoryGolangPackage) {
            notationMap.put(DIR_KEY, LocalDirectoryGolangPackage.class.cast(pkg).getDir());
        }
    }
}
