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
        if (supportJsonOutput()) {
            return jsonGoTestResultExtractor.extractTestResult(result);
        } else {
            return plainGoTestResultExtractor.extractTestResult(result);
        }
    }

    @Override
    public List<String> testParams() {
        if (supportJsonOutput()) {
            return jsonGoTestResultExtractor.testParams();
        } else {
            return plainGoTestResultExtractor.testParams();
        }
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    private boolean supportJsonOutput() {
        String[] version = goBinaryManager.getGoVersion().split("\\.");
        if (version.length < 2) {
            return false;
        }
        if (Integer.parseInt(version[0]) == 1) {
            return Integer.parseInt(version[1]) >= 10;
        }
        return Integer.parseInt(version[0]) > 1;
    }
}
