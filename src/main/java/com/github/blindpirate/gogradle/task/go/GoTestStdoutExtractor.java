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

import com.github.blindpirate.gogradle.util.IOUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult;
import org.gradle.api.internal.tasks.testing.junit.result.TestMethodResult;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.testing.TestResult;

import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.blindpirate.gogradle.util.DateUtils.toMilliseconds;
import static java.lang.Double.parseDouble;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Singleton
public class GoTestStdoutExtractor {
    private static final Logger LOGGER = Logging.getLogger(GoTestStdoutExtractor.class);
    private static final Map<String, TestResult.ResultType> RESULT_TYPE_MAP =
            ImmutableMap.of("PASS", TestResult.ResultType.SUCCESS,
                    "FAIL", TestResult.ResultType.FAILURE,
                    "SKIP", TestResult.ResultType.SKIPPED);

    //=== RUN   TestDiffToHTML
    //--- PASS: TestDiffToHTML (0.00s)
    private static final Pattern TEST_START_LINE_PATTERN
            = Pattern.compile("=== RUN\\s+(\\w+)(/?)");
    private static final String TEST_START = "=== RUN";
    private static final String TEST_PASS = "--- PASS";
    private static final String TEST_FAIL = "--- FAIL";
    private static final String TEST_SKIP = "--- SKIP";

    private static final String SETUP_FAILED_ERROR = "[setup failed]";
    private static final String BUILD_FAILED_ERROR = "[build failed]";
    private static final String CANNOT_LOAD_PACKAGE_ERROR = "can't load package";
    private static final String CANNOT_FIND_PACKAGE_ERROR = "cannot find package";
    private static final String RETURN_NON_ZERO = "go test return ";
    private static final AtomicLong GLOBAL_COUNTER = new AtomicLong(0);

    public List<TestClassResult> extractTestResult(PackageTestResult result) {
        if (stdoutContains(result, SETUP_FAILED_ERROR)) {
            return failResult(result, SETUP_FAILED_ERROR);
        } else if (stdoutContains(result, BUILD_FAILED_ERROR)) {
            return failResult(result, BUILD_FAILED_ERROR);
        } else if (stdoutContains(result, CANNOT_LOAD_PACKAGE_ERROR)) {
            return failResult(result, CANNOT_LOAD_PACKAGE_ERROR);
        } else if (stdoutContains(result, CANNOT_FIND_PACKAGE_ERROR)) {
            return failResult(result, CANNOT_FIND_PACKAGE_ERROR);
        } else if (testFailed(result)) {
            return failResult(result, RETURN_NON_ZERO + result.getCode());
        } else {
            return successfulTestResults(result);
        }
    }

    private boolean testFailed(PackageTestResult result) {
        return !stdoutContains(result, TEST_START) && result.getCode() != 0;
    }

    private boolean stdoutContains(PackageTestResult result, String error) {
        return result.getStdout().stream().anyMatch(s -> s.contains(error));
    }

    private List<TestClassResult> successfulTestResults(PackageTestResult result) {
        List<String> stdout = removeTailMessages(result.getStdout());

        Map<File, String> testFileContents = loadTestFiles(result.getTestFiles());
        List<GoTestMethodResult> results = extractTestMethodResult(stdout);

        Map<File, List<GoTestMethodResult>> testFileToResults = groupByTestFile(results, testFileContents);

        return testFileToResults.entrySet().stream()
                .map(entry -> {
                    String className = determineClassName(result.getPackagePath(), entry.getKey().getName());
                    return methodResultsToClassResult(className, entry.getValue());
                })
                .collect(toList());
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

    private List<TestClassResult> failResult(PackageTestResult testResult, String reason) {
        String message = String.join("\n", testResult.getStdout());
        GoTestMethodResult ret = new GoTestMethodResult(GLOBAL_COUNTER.incrementAndGet(),
                reason,
                TestResult.ResultType.FAILURE,
                0L,
                0L,
                message);

        ret.addFailure(message, message, message);

        String className = determineClassName(testResult.getPackagePath(), reason);

        return asList(methodResultsToClassResult(className, asList(ret)));
    }

    private List<GoTestMethodResult> extractTestMethodResult(List<String> stdout) {
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
        long id = GLOBAL_COUNTER.incrementAndGet();
        String message = String.join("\n", resultLines);

        for (String line : resultLines) {
            if (line.contains(TEST_PASS) || line.contains(TEST_FAIL) || line.contains(TEST_SKIP)) {
                Matcher matcher = testEndLinePattern.matcher(line);
                if (matcher.find()) {
                    TestResult.ResultType resultType = RESULT_TYPE_MAP.get(matcher.group(1));
                    long duration = toMilliseconds(parseDouble(matcher.group(2)));
                    return createTestMethodResult(id, testMethodName, resultType, message, duration);
                }
            }
        }

        return createTestMethodResult(id, testMethodName, TestResult.ResultType.FAILURE, message, 0L);
    }

    private GoTestMethodResult createTestMethodResult(long id,
                                                      String methodName,
                                                      TestResult.ResultType resultType,
                                                      String message,
                                                      long duration) {
        GoTestMethodResult result = new GoTestMethodResult(id,
                methodName,
                resultType,
                duration,
                0L,
                message);
        if (TestResult.ResultType.FAILURE == resultType) {
            result.addFailure(message, message, message);
        }
        return result;
    }

    private Map<File, String> loadTestFiles(List<File> testFiles) {
        return testFiles.
                stream()
                .collect(toMap(Function.identity(), IOUtils::toString));
    }

    private TestClassResult methodResultsToClassResult(String className,
                                                       List<GoTestMethodResult> methodResults) {
        TestClassResult ret = new TestClassResult(GLOBAL_COUNTER.incrementAndGet(), className, 0L);
        methodResults.forEach(ret::add);
        return ret;
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    private String determineClassName(String packagePath, String fileName) {
        String escapedPackagePath = IOUtils.encodeInternally(packagePath);
        escapedPackagePath = escapedPackagePath.replaceAll("\\.", "_DOT_");

        return escapedPackagePath.replaceAll("%2F", ".") + "." + fileName.replaceAll("\\.", "_DOT_");
    }

    private Map<File, List<GoTestMethodResult>> groupByTestFile(List<GoTestMethodResult> results,
                                                                Map<File, String> testFiles) {
        return results.stream()
                .collect(groupingBy(
                        result -> findTestFileOfMethod(testFiles, result.getName())
                ));
    }

    private File findTestFileOfMethod(Map<File, String> testFileContents, String methodName) {
        LOGGER.debug("trying to find {} in test files.");
        return testFileContents
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().contains(methodName))
                .findFirst()
                .get()
                .getKey();
    }

    public static class GoTestMethodResult extends TestMethodResult {
        private String message;

        public String getMessage() {
            return message;
        }

        public GoTestMethodResult(long id,
                                  String name,
                                  TestResult.ResultType resultType,
                                  long duration,
                                  long endTime,
                                  String message) {
            super(id, name, resultType, duration, endTime);
            this.message = message;
        }
    }
}
