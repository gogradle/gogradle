package com.github.blindpirate.gogradle.task.go.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
class GoTestResultJsonModel {
    private static final Set<String> VALID_ACTIONS = ImmutableSet.of("run", "output", "pass", "fail", "skip");
    @JsonProperty("Time")
    private String time;
    @JsonProperty("Action")
    private String action;
    @JsonProperty("Package")
    private String packageName;
    @JsonProperty("Test")
    private String test;
    @JsonProperty("Output")
    private String output;
    @JsonProperty("Elapsed")
    private Double elapsed;

    GoTestEvent toTestEvent() {
        if (VALID_ACTIONS.contains(action)) {
            return new GoTestEvent(test, output, action == null ? null : action.toLowerCase(), elapsed);
        } else {
            return null;
        }
    }
}
