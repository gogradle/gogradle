package com.github.blindpirate.gogradle.core.exceptions;

import java.io.IOException;
import java.nio.file.Path;

public class BuildException extends RuntimeException {
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
}
