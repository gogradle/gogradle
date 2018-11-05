package com.github.blindpirate.gogradle.task.go.test;

import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.task.go.PackageTestResult;
import org.gradle.api.internal.tasks.testing.junit.result.TestClassResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class DefaultGoTestResultExtractor implements GoTestResultExtractor {
    private PlainGoTestResultExtractor plainGoTestResultExtractor;
    private JsonGoTestResultExtractor jsonGoTestResultExtractor;
    private GoBinaryManager goBinaryManager;

    @Inject
    public DefaultGoTestResultExtractor(PlainGoTestResultExtractor plainExtractor,
                                        JsonGoTestResultExtractor jsonExtractor,
                                        GoBinaryManager binaryManager) {
        this.plainGoTestResultExtractor = plainExtractor;
        this.jsonGoTestResultExtractor = jsonExtractor;
        this.goBinaryManager = binaryManager;
    }

    @Override
    public List<TestClassResult> extractTestResult(PackageTestResult result) {
        if (goBinaryManager.supportTestJsonOutput()) {
            return jsonGoTestResultExtractor.extractTestResult(result);
        } else {
            return plainGoTestResultExtractor.extractTestResult(result);
        }
    }
}
