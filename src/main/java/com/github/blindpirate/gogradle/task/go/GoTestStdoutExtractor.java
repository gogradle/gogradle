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
                    "FAIL", TestResult.ResultType.FAILURE);

    //=== RUN   TestDiffToHTML
    //--- PASS: TestDiffToHTML (0.00s)
    private static final Pattern TEST_RESULT_PATTERN =
            Pattern.compile("=== RUN\\s+(\\w+)\\n((?:.|\\n)*?)--- (PASS|FAIL):\\s+\\w+\\s+\\(((\\d+)(\\.\\d+)?)s\\)");

    private static final String SETUP_FAILED_ERROR = "[setup failed]";
    private static final AtomicLong GLOBAL_COUNTER = new AtomicLong(0);

    public List<TestClassResult> extractTestResult(PackageTestContext context) {
        if (context.getStdout().contains(SETUP_FAILED_ERROR)) {
            return setupFailedResult(context);
        } else {
            return successfulTestResults(context);
        }
    }

    private List<TestClassResult> successfulTestResults(PackageTestContext context) {
        Map<File, String> testFileContents = loadTestFiles(context.getTestFiles());
        List<TestMethodResult> results = extractTestMethodResult(context.getStdout());

        Map<File, List<TestMethodResult>> testFileToResults = groupByTestFile(results, testFileContents);

        return testFileToResults.entrySet().stream()
                .map(entry -> {
                    String className = determineClassName(context.getPackagePath(), entry.getKey());
                    return methodResultsToClassResult(context.getPackagePath(), className, entry.getValue());
                })
                .collect(toList());
    }

    private List<TestClassResult> setupFailedResult(PackageTestContext context) {
        GoTestMethodResult result = new GoTestMethodResult(GLOBAL_COUNTER.incrementAndGet(),
                SETUP_FAILED_ERROR,
                TestResult.ResultType.FAILURE,
                0L, 0L, context.getStdout()
        );

        return asList(methodResultsToClassResult(context.getPackagePath(), SETUP_FAILED_ERROR, asList(result)));
    }

    private List<TestMethodResult> extractTestMethodResult(String stdout) {
        Matcher matcher = TEST_RESULT_PATTERN.matcher(stdout);
        List<TestMethodResult> ret = new ArrayList<>();
        while (matcher.find()) {
            long id = GLOBAL_COUNTER.incrementAndGet();
            String methodName = matcher.group(1);
            String message = matcher.group(2);
            TestResult.ResultType resultType = RESULT_TYPE_MAP.get(matcher.group(3));
            long duration = toMilliseconds(parseDouble(matcher.group(4)));

            TestMethodResult result = new GoTestMethodResult(id,
                    methodName,
                    resultType,
                    duration,
                    0L,
                    message);
            if (TestResult.ResultType.FAILURE == resultType) {
                result.addFailure(message, message, message);
            }
            ret.add(result);
        }
        return ret;
    }

    private Map<File, String> loadTestFiles(List<File> testFiles) {
        return testFiles.
                stream()
                .collect(toMap(Function.identity(), IOUtils::toString));
    }

    private TestClassResult methodResultsToClassResult(String packagePath,
                                                       String className,
                                                       List<TestMethodResult> methodResults) {
        TestClassResult ret = new TestClassResult(GLOBAL_COUNTER.incrementAndGet(), className, 0L);
        methodResults.forEach(ret::add);
        return ret;
    }

    private String determineClassName(String packagePath, File testFile) {
        String escapedPackagePath = IOUtils.encodeInternally(packagePath);
        escapedPackagePath = escapedPackagePath.replaceAll("\\.", "%2E");

        String fileName = testFile.getName();
        String nameWithoutDotGo = testFile.getName().substring(0, fileName.length() - 3);
        nameWithoutDotGo = nameWithoutDotGo.replaceAll("\\.", "%2E");

        return escapedPackagePath.replaceAll("%2F", ".") + "." + nameWithoutDotGo;
    }

    private Map<File, List<TestMethodResult>> groupByTestFile(List<TestMethodResult> results,
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
