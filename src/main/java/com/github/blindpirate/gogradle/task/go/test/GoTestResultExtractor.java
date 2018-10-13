package com.github.blindpirate.gogradle.task.go.test;

import com.github.blindpirate.gogradle.task.go.PackageTestResult;
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult;

import java.util.List;

/**
 * Extract test results from go test stdout.
 */
public interface GoTestResultExtractor {
    List<TestClassResult> extractTestResult(PackageTestResult result);

    List<String> testParams();
}
