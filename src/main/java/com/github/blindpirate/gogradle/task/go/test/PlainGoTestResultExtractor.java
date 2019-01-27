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

package com.github.blindpirate.gogradle.task.go.test;

import com.github.blindpirate.gogradle.task.go.PackageTestResult;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.tasks.testing.TestResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static java.lang.Double.parseDouble;

@Deprecated
public class PlainGoTestResultExtractor extends AbstractGoTestResultExtractor {
    private static final Map<String, TestResult.ResultType> RESULT_TYPE_MAP =
            ImmutableMap.of("PASS", TestResult.ResultType.SUCCESS,
                    "FAIL", TestResult.ResultType.FAILURE,
                    "SKIP", TestResult.ResultType.SKIPPED);

    //=== RUN   TestDiffToHTML
    //--- PASS: TestDiffToHTML (0.00s)
    private static final Pattern TEST_START_LINE_PATTERN
            = Pattern.compile("=== RUN\\s+(\\w+)(/?)");

    private static final String TEST_PASS = "--- PASS";
    private static final String TEST_FAIL = "--- FAIL";
    private static final String TEST_SKIP = "--- SKIP";

    protected List<GoTestMethodResult> extractMethodResults(PackageTestResult packageTestResult) {
        List<String> stdout = removeTailMessages(packageTestResult.getStdout());
        List<Pair<Integer, String>> testStartIndicesAndNames = extractStartIndiceAndTestMethodNames(stdout);

        List<GoTestMethodResult> ret = new ArrayList<>();
        for (int i = 0; i < testStartIndicesAndNames.size(); ++i) {
            int currentIndex = testStartIndicesAndNames.get(i).getLeft();
            String currentMethodName = testStartIndicesAndNames.get(i).getRight();
            int nextIndex = i == testStartIndicesAndNames.size() - 1
                    ? stdout.size()
                    : testStartIndicesAndNames.get(i + 1).getLeft();
            ret.add(extractOneTestMethod(currentMethodName, stdout.subList(currentIndex, nextIndex)));
        }

        return ret;
    }

    private List<String> removeTailMessages(List<String> stdout) {
        /*
            FAIL
            coverage: 66.7% of statements
            exit status 1
            FAIL github.com/my/project/a 0.006s

            FAIL
            exit status 1
            FAIL a 0.006s

            PASS
            coverage: 83.3% of statements
            ok a 0.005s

            PASS
            ok a 0.005s
         */

        for (int i = 1; i <= 4 && i <= stdout.size(); ++i) {
            String line = stdout.get(stdout.size() - i).trim();
            if ("FAIL".equals(line) || "PASS".equals(line)) {
                return stdout.subList(0, stdout.size() - i);
            }
        }
        return stdout;
    }

    private List<Pair<Integer, String>> extractStartIndiceAndTestMethodNames(List<String> stdout) {
        List<Pair<Integer, String>> ret = new ArrayList<>();

        for (int i = 0; i < stdout.size(); ++i) {
            Optional<String> testName = getRootTestNameFromLine(stdout.get(i));
            if (testName.isPresent()) {
                ret.add(Pair.of(i, testName.get()));
            }
        }
        return ret;
    }

    private Optional<String> getRootTestNameFromLine(String line) {
        // Extract test name only when it's a root test
        // Root test:      === RUN  TestThisIsRoot
        // Non-root test:  === RUN  TestThisIsRoot/SubTest
        line = line.trim();
        if (!line.startsWith(TEST_START)) {
            return Optional.empty();
        }
        Matcher matcher = TEST_START_LINE_PATTERN.matcher(line);
        if (matcher.find() && "".equals(matcher.group(2))) {
            return Optional.of(matcher.group(1));
        } else {
            return Optional.empty();
        }
    }

    private GoTestMethodResult extractOneTestMethod(String testMethodName, List<String> resultLines) {
        Pattern testEndLinePattern
                = Pattern.compile("--- (PASS|FAIL|SKIP):\\s+" + testMethodName + "\\s+\\(((\\d+)(\\.\\d+)?)s\\)");
        String message = String.join("\n", resultLines);

        for (String line : resultLines) {
            if (line.contains(TEST_PASS) || line.contains(TEST_FAIL) || line.contains(TEST_SKIP)) {
                Matcher matcher = testEndLinePattern.matcher(line);
                if (matcher.find()) {
                    TestResult.ResultType resultType = RESULT_TYPE_MAP.get(matcher.group(1));
                    long duration = toMilliseconds(parseDouble(matcher.group(2)));
                    return createTestMethodResult(testMethodName, resultType, message, duration);
                }
            }
        }
        return createTestMethodResult(testMethodName, TestResult.ResultType.FAILURE, message, 0L);
    }
}
