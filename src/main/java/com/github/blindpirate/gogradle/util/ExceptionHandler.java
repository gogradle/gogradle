package com.github.blindpirate.gogradle.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler {
    public static RuntimeException uncheckException(Throwable e) {
        return new IllegalStateException(e);
    }

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
