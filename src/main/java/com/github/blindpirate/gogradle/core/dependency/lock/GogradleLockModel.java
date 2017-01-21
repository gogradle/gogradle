package com.github.blindpirate.gogradle.core.dependency.lock;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.GogradleGlobal;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.build.Configuration.BUILD;
import static com.github.blindpirate.gogradle.build.Configuration.TEST;

public class GogradleLockModel {
    @JsonProperty("apiVersion")
    private String apiVersion;
    @JsonProperty("dependencies")
    private Map<String, List<Map<String, Object>>> dependencies;

    public static GogradleLockModel of(List<Map<String, Object>> buildNotations,
                                       List<Map<String, Object>> testNotations) {
        GogradleLockModel ret = new GogradleLockModel();
        ret.apiVersion = GogradleGlobal.GOGRADLE_VERSION;
        ret.dependencies = ImmutableMap.of(BUILD.getName(), buildNotations,
                TEST.getName(), testNotations);
        return ret;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public List<Map<String, Object>> getDependencies(String configurationName) {
        return dependencies.get(configurationName);
    }
}
