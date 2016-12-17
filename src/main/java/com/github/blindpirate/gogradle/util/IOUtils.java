package com.github.blindpirate.gogradle.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class IOUtils {
    public static String toString(File file) {
        try {
            return org.apache.commons.io.IOUtils.toString(new FileInputStream(file), "UTF8");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
