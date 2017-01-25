package com.github.blindpirate.gogradle.core.exceptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class BuildException extends RuntimeException {
    private BuildException(String msg) {
        super(msg);
    }

    private BuildException(Throwable e) {
        super(e);
    }

    private BuildException(String msg, Throwable e) {
        super(msg, e);
    }

    public static BuildException cannotCreateSymbolicLink(Path path, IOException e) {
        return new BuildException("Create symbolic link at " + path.toString()
                + " failed, does your system support it?", e);
    }

    public static BuildException cannotRenameVendorDir(File dotVendorDir) {
        return new BuildException("Renaming to " + dotVendorDir + " failed, cannot build or test");
    }

    public static BuildException processReturnNonZero(int retCode) {
        return new BuildException("Build failed due to non-zero return code of go process: " + retCode);
    }
}
