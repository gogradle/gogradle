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

package com.github.blindpirate.gogradle.util;


import com.google.common.collect.Lists;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Singleton
public class ProcessUtils {
    private static final Logger LOGGER = Logging.getLogger(ProcessUtils.class);

    public static class ProcessResult {
        private static ExecutorService threadPool = Executors.newCachedThreadPool();
        private int code;
        private String stdout;
        private String stderr;

        public int getCode() {
            return code;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        private ProcessResult(int code, String stdout, String stderr) {
            this.code = code;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        private static ProcessResult waitForFinish(Process process) throws InterruptedException, ExecutionException {
            Future<Integer> code = threadPool.submit((Callable<Integer>) process::waitFor);
            Future<String> stdout = threadPool.submit(() -> IOUtils.toString(process.getInputStream()));
            Future<String> stderr = threadPool.submit(() -> IOUtils.toString(process.getErrorStream()));
            return new ProcessResult(code.get(), stdout.get(), stderr.get());
        }
    }


    public String getStdout(Process process) {
        String ret = getResult(process).getStdout();
        LOGGER.debug("Process stdout: {}", ret);
        return ret;
    }

    public String runAndGetStdout(String... args) {
        return getStdout(run(args));
    }

    public String runAndGetStderr(String... args) {
        return getResult(run(args)).stderr;
    }

    public String runAndGetStdout(File workingDir, String... args) {
        return getStdout(run(Arrays.asList(args), null, workingDir));
    }

    public Process run(String... args) {
        return run(Arrays.asList(args));
    }

    public Process run(List<String> args) {
        return run(args, null, null);
    }

    public ProcessResult getResult(Process process) {
        try {
            return ProcessResult.waitForFinish(process);
        } catch (InterruptedException | ExecutionException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    public Process run(List<String> args, Map<String, String> envs, File workingDirectory) {
        LOGGER.debug("Forking process: args {}, envs {}, workingDir {}", args, envs, workingDirectory);
        try {
            ProcessBuilder pb = new ProcessBuilder().command(args);
            if (envs != null) {
                pb.environment().putAll(envs);
            }
            if (workingDirectory != null) {
                pb.directory(workingDirectory);
            }
            return pb.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // this should be moved to test source set since it's only used in test
    public ProcessResult runProcessWithCurrentClasspath(Class mainClass,
                                                        List<String> args,
                                                        Map<String, String> envs) {
        String currentClasspath = System.getProperty("java.class.path");

        List<String> cmds = Lists.newArrayList("java", "-cp", currentClasspath, mainClass.getName());
        cmds.addAll(args);
        return getResult(run(cmds, envs, null));
    }
}
