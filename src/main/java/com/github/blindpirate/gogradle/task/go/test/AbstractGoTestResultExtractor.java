package com.github.blindpirate.gogradle.task.go.test;

import com.github.blindpirate.gogradle.task.go.PackageTestResult;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult;
import org.gradle.api.internal.tasks.testing.junit.result.TestMethodResult;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.testing.TestResult;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public abstract class AbstractGoTestResultExtractor implements GoTestResultExtractor {
    private static final Logger LOGGER = Logging.getLogger(AbstractGoTestResultExtractor.class);
    private static final String SETUP_FAILED_ERROR = "[setup failed]";
    private static final String BUILD_FAILED_ERROR = "[build failed]";
    private static final String CANNOT_LOAD_PACKAGE_ERROR = "can't load package";
    private static final String CANNOT_FIND_PACKAGE_ERROR = "cannot find package";
    private static final String RETURN_NON_ZERO = "go test return ";
    protected static final String TEST_START = "=== RUN";

    protected static final AtomicLong GLOBAL_COUNTER = new AtomicLong(0);

    protected abstract List<GoTestMethodResult> extractMethodResults(PackageTestResult packageTestResult);

    protected TestClassResult methodResultsToClassResult(String className, List<GoTestMethodResult> methodResults) {
        TestClassResult ret = new TestClassResult(GLOBAL_COUNTER.incrementAndGet(), className, 0L);
        methodResults.forEach(ret::add);
        return ret;
    }

    @Override
    public List<TestClassResult> extractTestResult(PackageTestResult result) {
        if (stdoutContains(result, SETUP_FAILED_ERROR)) {
            return testFailToStartUpResult(result, SETUP_FAILED_ERROR);
        } else if (stdoutContains(result, BUILD_FAILED_ERROR)) {
            return testFailToStartUpResult(result, BUILD_FAILED_ERROR);
        } else if (stdoutContains(result, CANNOT_LOAD_PACKAGE_ERROR)) {
            return testFailToStartUpResult(result, CANNOT_LOAD_PACKAGE_ERROR);
        } else if (stdoutContains(result, CANNOT_FIND_PACKAGE_ERROR)) {
            return testFailToStartUpResult(result, CANNOT_FIND_PACKAGE_ERROR);
        } else if (testFailed(result)) {
            return testFailToStartUpResult(result, RETURN_NON_ZERO + result.getCode());
        } else {
            return testStartUpSuccessfullyResult(result);
        }
    }

    private List<TestClassResult> testStartUpSuccessfullyResult(PackageTestResult result) {
        Map<File, String> testFileContents = loadTestFiles(result.getTestFiles());
        List<GoTestMethodResult> results = extractMethodResults(result);

        Map<File, List<GoTestMethodResult>> testFileToResults = groupByTestFile(results, testFileContents);

        return testFileToResults.entrySet().stream()
                .map(entry -> {
                    String className = determineClassName(result.getPackagePath(), entry.getKey().getName());
                    return methodResultsToClassResult(className, entry.getValue());
                })
                .collect(toList());
    }

    private Map<File, String> loadTestFiles(List<File> testFiles) {
        return testFiles.
                stream()
                .collect(toMap(Function.identity(), IOUtils::toString));
    }

    private Map<File, List<GoTestMethodResult>> groupByTestFile(List<GoTestMethodResult> results,
                                                                Map<File, String> testFiles) {
        return results.stream()
                .collect(groupingBy(
                        result -> findTestFileOfMethod(testFiles, result.getName()),
                        LinkedHashMap::new,
                        toList()
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

    private boolean stdoutContains(PackageTestResult result, String error) {
        return result.getStdout().stream().anyMatch(s -> s.contains(error));
    }

    private boolean testFailed(PackageTestResult result) {
        return !stdoutContains(result, TEST_START) && result.getCode() != 0;
    }

    private List<TestClassResult> testFailToStartUpResult(PackageTestResult testResult, String reason) {
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


    @SuppressWarnings({"checkstyle:magicnumber"})
    protected String determineClassName(String packagePath, String fileName) {
        String escapedPackagePath = IOUtils.encodeInternally(packagePath);
        escapedPackagePath = escapedPackagePath.replaceAll("\\.", "_DOT_");

        return escapedPackagePath.replaceAll("%2F", ".") + "." + fileName.replaceAll("\\.", "_DOT_");
    }

    protected GoTestMethodResult createTestMethodResult(String methodName,
                                                        TestResult.ResultType resultType,
                                                        String message,
                                                        long duration) {
        GoTestMethodResult result = new GoTestMethodResult(GLOBAL_COUNTER.incrementAndGet(),
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
