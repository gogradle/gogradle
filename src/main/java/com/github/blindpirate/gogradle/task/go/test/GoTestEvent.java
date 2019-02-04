package com.github.blindpirate.gogradle.task.go.test;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static com.github.blindpirate.gogradle.task.go.test.AbstractGoTestResultExtractor.TEST_FAIL;
import static com.github.blindpirate.gogradle.task.go.test.AbstractGoTestResultExtractor.TEST_PASS;
import static com.github.blindpirate.gogradle.task.go.test.AbstractGoTestResultExtractor.TEST_START;
import static com.github.blindpirate.gogradle.task.go.test.JsonGoTestResultExtractor.TEST_NAME_IN_OUTPUT_PATTERN;
import static org.gradle.api.tasks.testing.TestResult.ResultType;

class GoTestEvent {
    //    {"Time":"2018-10-12T19:57:48.727973+08:00","Action":"run","Package":"a","Test":"Test_A1_1"}
    //    {"Time":"2018-10-12T19:57:48.728171+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"=== RUN   Test_A1_1\n"}
    //    {"Time":"2018-10-12T19:57:48.728187+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"--- FAIL: Test_A1_1 (0.00s)\n"}
    //    {"Time":"2018-10-12T19:57:48.728193+08:00","Action":"output","Package":"a","Test":"Test_A1_1","Output":"    a1_test.go:9: Failed\n"}
    //    {"Time":"2018-10-12T19:57:48.728209+08:00","Action":"fail","Package":"a","Test":"Test_A1_1","Elapsed":0}
    private static final Map<String, ResultType> RESULT_TYPES = ImmutableMap.of(
            "pass", ResultType.SUCCESS,
            "fail", ResultType.FAILURE,
            "skip", ResultType.SKIPPED);
    private String testName;
    private String output;
    private ResultType resultType;
    private Long durationMillis;

    GoTestEvent(String testName, String output, String action, Double elapsed) {
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

    String getTestName() {
        return testName;
    }

    ResultType getResultType() {
        return resultType;
    }

    String getOutput() {
        return output;
    }

    Long getDurationMillis() {
        return durationMillis;
    }

    // not package result
    boolean hasTestName() {
        return testName != null;
    }
}

