package com.github.blindpirate.gogradle.util;

public class ExceptionHandler {
    public static RuntimeException uncheckException(Throwable e) {
        return new IllegalStateException(e);
    }
}
