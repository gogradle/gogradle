package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.util.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult;
import org.gradle.api.internal.tasks.testing.junit.result.TestResultsProvider;
import org.gradle.api.tasks.testing.TestOutputEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.task.go.GoTestStdoutExtractor.GoTestMethodResult;

public class GoTestResultsProvider implements TestResultsProvider {
    private List<TestClassResult> testClassResults = new ArrayList<>();

    private Map<Long, TestClassResult> idToClassResultMap = new HashMap<>();

    public GoTestResultsProvider(List<TestClassResult> results) {
        testClassResults.addAll(results);
        testClassResults.forEach(result -> idToClassResultMap.put(result.getId(), result));
    }

    @Override
    public void writeAllOutput(long id, TestOutputEvent.Destination destination, Writer writer) {
        TestClassResult result = idToClassResultMap.get(id);
        String stdout = result.getResults().stream()
                .map(methodResult -> (GoTestMethodResult) methodResult)
                .map(GoTestMethodResult::getMessage)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining("\n"));
        try {
            writer.write(stdout);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeNonTestOutput(long id, TestOutputEvent.Destination destination, Writer writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTestOutput(long classId, long testId, TestOutputEvent.Destination destination, Writer writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitClasses(Action<? super TestClassResult> visitor) {
        testClassResults.forEach(visitor::execute);
    }

    @Override
    public boolean hasOutput(long id, TestOutputEvent.Destination destination) {
        if (destination == TestOutputEvent.Destination.StdErr) {
            return false;
        }
        TestClassResult result = idToClassResultMap.get(id);
        return result.getResults().stream()
                .map(methodResult -> (GoTestMethodResult) methodResult)
                .map(GoTestMethodResult::getMessage)
                .anyMatch(StringUtils::isNotEmpty);
    }

    @Override
    public boolean isHasResults() {
        return !testClassResults.isEmpty();
    }

    @Override
    public void close() throws IOException {
    }
}
