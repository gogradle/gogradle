package com.github.blindpirate.gogradle.util;


import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProcessUtils {
    public static class ProcessResult {
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

        public ProcessResult(Process process) throws InterruptedException {
            code = process.waitFor();
            stdout = IOUtils.toString(process.getInputStream());
            stderr = IOUtils.toString(process.getErrorStream());
        }
    }

    public static ProcessResult run(List<String> args, Map<String, String> envs) {
        return run(args, envs, null);
    }

    public static ProcessResult run(List<String> args, Map<String, String> envs, File workingDirectory) {
        try {
            ProcessBuilder pb = new ProcessBuilder().command(args);
            pb.environment().putAll(envs);
            if (workingDirectory != null) {
                pb.directory(workingDirectory);
            }
            Process process = pb.start();
            return new ProcessResult(process);
        } catch (IOException | InterruptedException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }


    public static ProcessResult runProcessWithCurrentClasspath(Class mainClass,
                                                               List<String> args,
                                                               Map<String, String> envs) {
        String currentClasspath = System.getProperty("java.class.path");

        List<String> cmds = Lists.newArrayList("java", "-cp", currentClasspath, mainClass.getName());
        cmds.addAll(args);
        return run(cmds, envs);
    }
}
