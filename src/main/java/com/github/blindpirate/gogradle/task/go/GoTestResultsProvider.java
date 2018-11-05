/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

import static com.github.blindpirate.gogradle.task.go.test.PlainGoTestResultExtractor.GoTestMethodResult;

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
