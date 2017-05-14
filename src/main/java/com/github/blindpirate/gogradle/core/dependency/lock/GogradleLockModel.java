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

package com.github.blindpirate.gogradle.core.dependency.lock;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.common.WithApiVersion;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.GolangConfiguration.TEST;

public class GogradleLockModel extends WithApiVersion {
    @JsonProperty("dependencies")
    private Map<String, List<Map<String, Object>>> dependencies;

    public static GogradleLockModel of(List<Map<String, Object>> buildNotations,
                                       List<Map<String, Object>> testNotations) {
        GogradleLockModel ret = new GogradleLockModel();
        ret.dependencies = ImmutableMap.of(BUILD, buildNotations,
                TEST, testNotations);
        return ret;
    }

    public List<Map<String, Object>> getDependencies(String configurationName) {
        return dependencies.get(configurationName);
    }
}
