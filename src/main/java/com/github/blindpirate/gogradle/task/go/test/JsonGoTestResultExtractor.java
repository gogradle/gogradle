package com.github.blindpirate.gogradle.task.go.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.task.go.PackageTestResult;
import com.github.blindpirate.gogradle.util.DataExchange;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.gradle.api.tasks.testing.TestResult.ResultType;

/**
 * Extract results from JSON outputs. Supported on Go 1.10+.
 */
public class JsonGoTestResultExtractor extends AbstractGoTestResultExtractor {
    private static final Logger LOGGER = Logging.getLogger(JsonGoTestResultExtractor.class);
    // === RUN   TestCounter_Inc/add_2_to_inc1.ticker[\"B\"]_value:_3
    // === RUN   TestCounter_Inc
    // --- PASS: TestCounter_Inc (0.00s)
    // --- PASS: TestCounter_Inc/add_1_to_inc1.count[\"A\"]_value:_1 (0.00s)
    private static final Pattern TEST_NAME_IN_OUTPUT_PATTERN
            = Pattern.compile("(?:===|---) (RUN|PASS|SKIP|FAIL):?\\s+([^\\s]+)(?:\\s+\\(((\\d+)(\\.\\d+)?)s\\))?");

    @Override
    protected List<GoTestMethodResult> extractMethodResults(PackageTestResult packageTestResult) {
        Map<String, List<GoTestEvent>> jsonModels = packageTestResult.getStdout()
                .stream()
                .map(this::tryConvertToEvent)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(GoTestEvent::hasTestName)
                .collect(groupingBy(GoTestEvent::getTestName, LinkedHashMap::new, toList()));

        return jsonModels.entrySet().stream().map(this::convertToMethodResult).collect(toList());
    }

    private GoTestMethodResult convertToMethodResult(Map.Entry<String, List<GoTestEvent>> entry) {
        String testName = entry.getKey();
        List<GoTestEvent> testEvents = entry.getValue();
        ResultType resultType = testEvents
                .stream()
                .map(GoTestEvent::getResultType)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);

        String message = testEvents.stream()
                .map(GoTestEvent::getOutput)
                .collect(joining("\n"));
        long duration = testEvents
                .stream()
                .map(GoTestEvent::getDurationMillis)
                .filter(Objects::nonNull)
                .findAny()
                .get();

        return createTestMethodResult(testName, resultType, message, duration);
    }

    private Optional<GoTestEvent> tryConvertToEvent(String line) {
        return tryConvertToJsonModel(line).map(GoTestResultJsonModel::toTestEvent);
    }

    private Optional<GoTestResultJsonModel> tryConvertToJsonModel(String line) {
        try {
            return Optional.of(DataExchange.parseJson(line, GoTestResultJsonModel.class));
        } catch (Exception e) {
            LOGGER.debug("Exception when processing line: " + line, e);
            return Optional.empty();
        }
    }

//    {"Time":"2018-10-12T19:57:48.727973+08:00","Action":"run","Package":"a","Test":"Test_A1_1"}
//    {"Time":"2018-10-12T19:57:48.728171+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"=== RUN   Test_A1_1\n"}
//    {"Time":"2018-10-12T19:57:48.728187+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"--- FAIL: Test_A1_1 (0.00s)\n"}
//    {"Time":"2018-10-12T19:57:48.728193+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"    a1_test.go:9: Failed\n"}
//    {"Time":"2018-10-12T19:57:48.728209+08:00","Action":"fail","Package":"a","Test":"Test_A1_1","Elapsed":0}

    private static class GoTestEvent {
        private static final Map<String, ResultType> RESULT_TYPES = ImmutableMap.of(
                "pass", ResultType.SUCCESS,
                "fail", ResultType.FAILURE,
                "skip", ResultType.SKIPPED);
        private String testName;
        private String output;
        private ResultType resultType;
        private Long durationMillis;

        private GoTestEvent(String testName, String output, String action, Double elapsed) {
            this.output = output;
            this.durationMillis = secondToMillis(elapsed);

            this.resultType = RESULT_TYPES.get(action == null ? null : action.toLowerCase());
            this.testName = testName;

            if (hasTestNameResultInOutput()) {
                initResultNameAndTypeFromOutput();
            }
        }

        private Long secondToMillis(Double second) {
            return second == null ? null : (long) (second * 1000);
        }

        private void initResultNameAndTypeFromOutput() {
            Matcher matcher = TEST_NAME_IN_OUTPUT_PATTERN.matcher(output);
            if (matcher.find()) {
                this.resultType = RESULT_TYPES.get(matcher.group(1).toLowerCase());
                this.testName = matcher.group(2);

                String duration = matcher.group(3);
                this.durationMillis = secondToMillis(duration == null ? null : Double.parseDouble(duration));
            } else {
                throw new IllegalStateException("Can't find test name from output: " + output);
            }
        }

        private boolean hasTestNameResultInOutput() {
            return Stream.of(TEST_START, TEST_FAIL, TEST_PASS, TEST_PASS)
                    .anyMatch(s -> output != null && output.contains(s));
        }

        private String getTestName() {
            return testName;
        }

        private ResultType getResultType() {
            return resultType;
        }

        private String getOutput() {
            return output;
        }

        private Long getDurationMillis() {
            return durationMillis;
        }

        // not package result
        private boolean hasTestName() {
            return testName != null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoTestResultJsonModel {
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

        private GoTestEvent toTestEvent() {
            if (VALID_ACTIONS.contains(action)) {
                return new GoTestEvent(test, output, action == null ? null : action.toLowerCase(), elapsed);
            } else {
                return null;
            }
        }
    }
}
