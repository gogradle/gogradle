package com.github.blindpirate.gogradle.task.go.test;

import com.github.blindpirate.gogradle.task.go.PackageTestResult;
import com.github.blindpirate.gogradle.util.DataExchange;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

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
    static final Pattern TEST_NAME_IN_OUTPUT_PATTERN
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
                .filter(Objects::nonNull)
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
}
