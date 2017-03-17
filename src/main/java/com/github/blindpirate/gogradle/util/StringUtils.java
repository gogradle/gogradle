package com.github.blindpirate.gogradle.util;


import groovy.text.GStringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class StringUtils {
    public static String removeEnd(String s, String suffix) {
        return org.apache.commons.lang3.StringUtils.removeEnd(s, suffix);
    }

    public static String[] splitAndTrim(String str, String regex) {
        String[] array = str.split(regex);
        return Stream.of(array)
                .map(org.apache.commons.lang3.StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

    public static boolean isNotBlank(String s) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(s);
    }

    public static boolean isBlank(String s) {
        return org.apache.commons.lang3.StringUtils.isBlank(s);
    }

    public static boolean allBlank(String... strs) {
        return Arrays.stream(strs).allMatch(StringUtils::isBlank);
    }

    public static boolean isEmpty(String s) {
        return org.apache.commons.lang3.StringUtils.isEmpty(s);
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static boolean fileNameStartsWithAny(File file, String... prefix) {
        return startsWithAny(file.getName(), prefix);
    }

    public static boolean fileNameEndsWithAny(File file, String... prefix) {
        return endsWithAny(file.getName(), prefix);
    }

    public static boolean startsWithAny(String str, String... prefix) {
        return Stream.of(prefix).anyMatch(str::startsWith);
    }

    public static boolean endsWithAny(String str, String... suffix) {
        return Stream.of(suffix).anyMatch(str::endsWith);
    }

    public static boolean fileNameEqualsAny(File file, String... name) {
        return Stream.of(name).anyMatch(file.getName()::equals);
    }

    public static String toUnixString(Path path) {
        return path.toString().replace("\\", "/");
    }

    public static String render(String template, Map<String, Object> context) {
        try {
            context = new HashMap<>(context);
            return new GStringTemplateEngine().createTemplate(template).make(context).toString();
        } catch (ClassNotFoundException | IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    public static int lastIndexOf(String str, String substr) {
        return org.apache.commons.lang3.StringUtils.lastIndexOf(str, substr);
    }

    public static String substring(String s, int start, int end) {
        return org.apache.commons.lang3.StringUtils.substring(s, start, end);
    }
}
