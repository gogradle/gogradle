package com.github.blindpirate.gogradle.task.go;

import java.io.File;
import java.util.List;

public class PackageTestContext {
    private String packagePath;
    private List<File> testFiles;
    private String stdout;

    public String getPackagePath() {
        return packagePath;
    }

    public List<File> getTestFiles() {
        return testFiles;
    }

    public String getStdout() {
        return stdout;
    }

    public static PackageTestContextBuilder builder() {
        return new PackageTestContextBuilder();
    }

    public static final class PackageTestContextBuilder {
        private String packagePath;
        private List<File> testFiles;
        private String stdout;

        private PackageTestContextBuilder() {
        }

        public PackageTestContextBuilder withPackagePath(String packagePath) {
            this.packagePath = packagePath;
            return this;
        }

        public PackageTestContextBuilder withTestFiles(List<File> testFiles) {
            this.testFiles = testFiles;
            return this;
        }

        public PackageTestContextBuilder withStdout(String stdout) {
            this.stdout = stdout;
            return this;
        }

        public PackageTestContext build() {
            PackageTestContext packageTestContext = new PackageTestContext();
            packageTestContext.stdout = this.stdout;
            packageTestContext.testFiles = this.testFiles;
            packageTestContext.packagePath = this.packagePath;
            return packageTestContext;
        }
    }
}
