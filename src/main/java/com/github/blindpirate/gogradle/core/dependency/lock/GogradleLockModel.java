package com.github.blindpirate.gogradle.core.dependency.lock;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.GogradleGlobal;

import java.util.List;
import java.util.Map;

public class GogradleLockModel {
    @JsonProperty("apiVersion")
    private String apiVersion;
    @JsonProperty("dependencies")
    private List<Map<String, Object>> dependencies;

    public static GogradleLockModel of(List<Map<String, Object>> notations) {
        GogradleLockModel ret = new GogradleLockModel();
        ret.apiVersion = GogradleGlobal.GOGRADLE_VERSION;
        ret.dependencies = notations;
        return ret;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public List<Map<String, Object>> getDependencies() {
        return dependencies;
    }
}
