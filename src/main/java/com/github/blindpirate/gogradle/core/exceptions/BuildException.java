package com.github.blindpirate.gogradle.core.exceptions;

import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildException extends RuntimeException {
    private BuildException(String msg) {
        super(msg);
    }

    private BuildException(String msg, Throwable e) {
        super(msg, e);
    }

    public static BuildException cannotCreateSymbolicLink(Path path, IOException e) {
        return new BuildException("Create symbolic link at " + path.toString()
                + " failed", e);
    }

    public static BuildException cannotRenameVendorDir(File dotVendorDir) {
        return new BuildException("Renaming to " + dotVendorDir + " failed, cannot build or test");
    }

    public static BuildException processInteractionFailed(int retCode, String message) {
        return new BuildException("Build failed due to return code " + retCode + " of: " + message);
    }

    public static BuildException processInteractionFailed(List<String> cmds,
                                                          Map<String, String> env,
                                                          File workingDir,
                                                          ProcessUtils.ProcessResult result) {
        env = new HashMap<>(env);
        env.put("PATH", System.getenv("PATH"));

        return new BuildException("Build failed due to return code " + result.getCode()
                + " of cmd: " + String.join(" ", cmds)
                + "\nstderr: " + result.getStderr()
                + "\nstdout: " + result.getStdout()
                + "\nin: " + workingDir.getAbsolutePath()
                + "\nwith env:\n" + StringUtils.formatEnv(env));
    }

    public static BuildException processInteractionFailed(List<String> cmds,
                                                          Map<String, String> env,
                                                          File workingDir,
                                                          int retcode) {
        env = new HashMap<>(env);
        env.put("PATH", System.getenv("PATH"));

        return new BuildException("Build failed due to return code " + retcode
                + " of cmd: " + String.join(" ", cmds)
                + "\nin: " + workingDir.getAbsolutePath()
                + "\nwith env:\n" + StringUtils.formatEnv(env));
    }

    public static BuildException processInteractionFailed(List<String> cmds,
                                                          Map<String, String> env,
                                                          File workingDir,
                                                          Throwable e) {
        env = new HashMap<>(env);
        env.put("PATH", System.getenv("PATH"));

        return new BuildException("Build failed due to exception"
                + " of cmd: " + String.join(" ", cmds)
                + "\nin: " + workingDir.getAbsolutePath()
                + "\nwith env:\n" + StringUtils.formatEnv(env)
                + "\nexception:" + ExceptionHandler.getStackTrace(e));
    }
}
