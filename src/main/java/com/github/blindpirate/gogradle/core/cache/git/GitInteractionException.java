package com.github.blindpirate.gogradle.core.cache.git;

public class GitInteractionException extends RuntimeException {
    public GitInteractionException(String message) {
        super(message);
    }

    public GitInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
