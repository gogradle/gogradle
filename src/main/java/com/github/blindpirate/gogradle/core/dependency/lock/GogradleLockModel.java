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
