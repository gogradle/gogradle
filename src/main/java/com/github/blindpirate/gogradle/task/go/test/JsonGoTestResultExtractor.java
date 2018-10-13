package com.github.blindpirate.gogradle.task.go.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.blindpirate.gogradle.task.go.PackageTestResult;
import com.github.blindpirate.gogradle.util.DataExchange;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.testing.TestResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extract results from JSON outputs. Supported on Go 1.10+.
 */
public class JsonGoTestResultExtractor extends AbstractGoTestResultExtractor {
    private static final Logger LOGGER = Logging.getLogger(JsonGoTestResultExtractor.class);

    @Override
    protected List<GoTestMethodResult> extractMethodResults(PackageTestResult packageTestResult) {
        Map<String, List<GoTestResultJsonModel>> jsonModels = packageTestResult.getStdout()
                .stream()
                .map(this::tryConvertToJson)
                .filter(Objects::nonNull)
                .filter(model -> model.getTest() != null) // package result
                .collect(Collectors.groupingBy(GoTestResultJsonModel::getTest, LinkedHashMap::new, Collectors.toList()));

        return jsonModels.entrySet().stream().map(this::convertToMethodResult).collect(Collectors.toList());
    }

    private GoTestMethodResult convertToMethodResult(Map.Entry<String, List<GoTestResultJsonModel>> entry) {
        String testName = entry.getKey();
        List<GoTestResultJsonModel> testEvents = entry.getValue();
        TestResult.ResultType resultType = getResultOrFallback(testEvents).action.toGradleResultType();
        String message = testEvents.stream()
                .filter(GoTestResultJsonModel.filter(ActionType.OUTPUT))
                .map(GoTestResultJsonModel::getOutput)
                .collect(Collectors.joining("\n"));
        long duration = getResultOrFallback(testEvents).getMillisecondsDuration();

        return createTestMethodResult(testName, resultType, message, duration);
    }

    private GoTestResultJsonModel getResultOrFallback(List<GoTestResultJsonModel> testEvents) {
        return testEvents.stream()
                .filter(GoTestResultJsonModel.filter(ActionType.PASS, ActionType.FAIL))
                .findAny()
                .orElseGet(GoTestResultJsonModel::new);
    }


    private GoTestResultJsonModel tryConvertToJson(String line) {
        try {
            return DataExchange.parseJson(line, GoTestResultJsonModel.class);
        } catch (Exception e) {
            LOGGER.debug("Exception when processing line: " + line, e);
            return null;
        }
    }

    @Override
    public List<String> testParams() {
        return Arrays.asList("test", "-v", "-json");
    }

    private enum ActionType {
        RUN, OUTPUT, PASS, FAIL;

        @JsonCreator
        private static ActionType from(String value) {
            return Stream.of(ActionType.values())
                    .filter(actionType -> actionType.toString().equalsIgnoreCase(value))
                    .findAny()
                    .get();
        }

        private TestResult.ResultType toGradleResultType() {
            switch (this) {
                case RUN:
                case OUTPUT:
                    return null;
                case PASS:
                    return TestResult.ResultType.SUCCESS;
                case FAIL:
                    return TestResult.ResultType.FAILURE;
                default:
                    return null;
            }
        }
    }

//    {"Time":"2018-10-12T19:57:48.727973+08:00","Action":"run","Package":"a","Test":"Test_A1_1"}
//    {"Time":"2018-10-12T19:57:48.728171+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"=== RUN   Test_A1_1\n"}
//    {"Time":"2018-10-12T19:57:48.728187+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"--- FAIL: Test_A1_1 (0.00s)\n"}
//    {"Time":"2018-10-12T19:57:48.728193+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"    a1_test.go:9: Failed\n"}
//    {"Time":"2018-10-12T19:57:48.728209+08:00","Action":"fail","Package":"a","Test":"Test_A1_1","Elapsed":0}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoTestResultJsonModel {
        @JsonProperty("Time")
        private String time;
        @JsonProperty("Action")
        private ActionType action = ActionType.PASS;
        @JsonProperty("Package")
        private String packageName;
        @JsonProperty("Test")
        private String test;
        @JsonProperty("Output")
        private String output;
        @JsonProperty("Elapsed")
        private double elapsed;

        public String getTest() {
            return test;
        }

        public String getOutput() {
            return output;
        }

        public long getMillisecondsDuration() {
            return (long) elapsed * 1000;
        }

        public static Predicate<GoTestResultJsonModel> filter(ActionType... actionTypes) {
            return model -> Arrays.asList(actionTypes).contains(model.action);
        }
    }

    private class GoTestResultJsonDeserializer extends JsonDeserializer<GoTestResultJsonModel> {

        @Override
        public GoTestResultJsonModel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return null;
        }
    }
}
