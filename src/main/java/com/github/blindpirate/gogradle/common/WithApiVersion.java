package com.github.blindpirate.gogradle.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.GogradleGlobal;

public abstract class WithApiVersion {
    @JsonProperty("apiVersion")
    private String apiVersion;

    protected WithApiVersion() {
        this.apiVersion = GogradleGlobal.GOGRADLE_VERSION;
    }

    public String getApiVersion() {
        return apiVersion;
    }
}
