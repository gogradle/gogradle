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

import java.io.File;
import java.util.List;

public class PackageTestResult {
    private String packagePath;
    private List<File> testFiles;
    private List<String> stdout;
    private int code;

    public int getCode() {
        return code;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public List<File> getTestFiles() {
        return testFiles;
    }

    public List<String> getStdout() {
        return stdout;
    }

    public static PackageTestResultBuilder builder() {
        return new PackageTestResultBuilder();
    }

    public static final class PackageTestResultBuilder {
        private String packagePath;
        private List<File> testFiles;
        private List<String> stdout;
        private int code;

        private PackageTestResultBuilder() {
        }

        public PackageTestResultBuilder withPackagePath(String packagePath) {
            this.packagePath = packagePath;
            return this;
        }

        public PackageTestResultBuilder withTestFiles(List<File> testFiles) {
            this.testFiles = testFiles;
            return this;
        }

        public PackageTestResultBuilder withStdout(List<String> stdout) {
            this.stdout = stdout;
            return this;
        }

        public PackageTestResultBuilder withCode(int code) {
            this.code = code;
            return this;
        }

        public PackageTestResult build() {
            PackageTestResult ret = new PackageTestResult();
            ret.stdout = this.stdout;
            ret.testFiles = this.testFiles;
            ret.packagePath = this.packagePath;
            ret.code = code;
            return ret;
        }
    }
}
