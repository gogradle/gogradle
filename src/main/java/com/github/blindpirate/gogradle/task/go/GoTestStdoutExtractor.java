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
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                    "FAIL", TestResult.ResultType.FAILURE);

    //=== RUN   TestDiffToHTML
    //--- PASS: TestDiffToHTML (0.00s)
    private static final Pattern TEST_END_LINE_PATTERN
            = Pattern.compile("--- (PASS|FAIL):\\s+(\\w+)\\s+\\(((\\d+)(\\.\\d+)?)s\\)");
    private static final String TEST_START = "=== RUN";
    private static final String TEST_PASS = "--- PASS";
    private static final String TEST_FAIL = "--- FAIL";

    private static final String SETUP_FAILED_ERROR = "[setup failed]";
    private static final String BUILD_FAILED_ERROR = "[build failed]";
    private static final String CANNOT_LOAD_PACKAGE_ERROR = "can't load package";
    private static final String CANNOT_FIND_PACKAGE_ERROR = "cannot find package";
    private static final AtomicLong GLOBAL_COUNTER = new AtomicLong(0);

    public List<TestClassResult> extractTestResult(PackageTestContext context) {
        if (stdoutContains(context, SETUP_FAILED_ERROR)) {
            return failResult(context, SETUP_FAILED_ERROR);
        } else if (stdoutContains(context, BUILD_FAILED_ERROR)) {
            return failResult(context, BUILD_FAILED_ERROR);
        } else if (stdoutContains(context, CANNOT_LOAD_PACKAGE_ERROR)) {
            return failResult(context, CANNOT_LOAD_PACKAGE_ERROR);
        } else if (stdoutContains(context, CANNOT_FIND_PACKAGE_ERROR)) {
            return failResult(context, CANNOT_FIND_PACKAGE_ERROR);
        } else {
            return successfulTestResults(context);
        }
    }

    private boolean stdoutContains(PackageTestContext context, String error) {
        return context.getStdout().stream().anyMatch(s -> s.contains(error));
    }

    private List<TestClassResult> successfulTestResults(PackageTestContext context) {
        List<String> stdout = removeTailMessages(context.getStdout());

        Map<File, String> testFileContents = loadTestFiles(context.getTestFiles());
        List<GoTestMethodResult> results = extractTestMethodResult(stdout);

        Map<File, List<GoTestMethodResult>> testFileToResults = groupByTestFile(results, testFileContents);

        return testFileToResults.entrySet().stream()
                .map(entry -> {
                    String className = determineClassName(context.getPackagePath(), entry.getKey().getName());
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

    private List<TestClassResult> failResult(PackageTestContext context, String reason) {
        String message = String.join("\n", context.getStdout());
        GoTestMethodResult result = new GoTestMethodResult(GLOBAL_COUNTER.incrementAndGet(),
                reason,
                TestResult.ResultType.FAILURE,
                0L,
                0L,
                message);

        result.addFailure(message, message, message);

        String className = determineClassName(context.getPackagePath(), reason);

        return asList(methodResultsToClassResult(className, asList(result)));
    }

    private List<GoTestMethodResult> extractTestMethodResult(List<String> stdout) {
        List<Integer> testStartLines = IntStream.range(0, stdout.size())
                .filter(line -> stdout.get(line).startsWith(TEST_START))
                .boxed()
                .collect(Collectors.toList());

        List<GoTestMethodResult> ret = new ArrayList<>();

        for (int i = 0; i < testStartLines.size(); ++i) {
            int testResultStart = testStartLines.get(i);
            int nextTestResultStart = i == testStartLines.size() - 1 ? stdout.size() : testStartLines.get(i + 1);
            List<String> middleLines = stdout.subList(testResultStart, nextTestResultStart);

            Optional<GoTestMethodResult> oneResult = extractOneTestMethod(middleLines);
            oneResult.ifPresent(ret::add);
        }

        return ret;
    }

    private Optional<GoTestMethodResult> extractOneTestMethod(List<String> middleLines) {
        OptionalInt testResultLineIndex = IntStream.range(0, middleLines.size())
                .filter(index -> middleLines.get(index).contains(TEST_FAIL)
                        || middleLines.get(index).contains(TEST_PASS))
                .findAny();
        if (testResultLineIndex.isPresent()) {
            String line = middleLines.get(testResultLineIndex.getAsInt());
            Matcher matcher = TEST_END_LINE_PATTERN.matcher(line);
            if (matcher.find()) {
                long id = GLOBAL_COUNTER.incrementAndGet();
                TestResult.ResultType resultType = RESULT_TYPE_MAP.get(matcher.group(1));
                String methodName = matcher.group(2);
                long duration = toMilliseconds(parseDouble(matcher.group(3)));

                String message = IntStream.range(1, middleLines.size())
                        .mapToObj(i -> mapToResultLine(i, middleLines, testResultLineIndex.getAsInt()))
                        .collect(Collectors.joining("\n"));

                GoTestMethodResult result = new GoTestMethodResult(id,
                        methodName,
                        resultType,
                        duration,
                        0L,
                        message);
                if (TestResult.ResultType.FAILURE == resultType) {
                    result.addFailure(message, message, message);
                }
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    private String mapToResultLine(int index, List<String> lines, int testResultLineIndex) {
        String line = lines.get(index);
        if (index == testResultLineIndex) {
            int testFailIndex = line.indexOf(TEST_FAIL);
            int testPassIndex = line.indexOf(TEST_PASS);
            if (testFailIndex > 0 || testPassIndex > 0) {
                // they seemed to omit a new line
                return line.substring(0, testFailIndex > 0 ? testFailIndex : testPassIndex);
            } else {
                return "";
            }
        }
        return lines.get(index);
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
