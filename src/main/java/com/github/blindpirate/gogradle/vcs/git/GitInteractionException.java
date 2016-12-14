package com.github.blindpirate.gogradle.vcs.git;

public class GitInteractionException extends RuntimeException {
    public GitInteractionException(String message) {
        super(message);
    }

    public GitInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
