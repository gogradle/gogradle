package com.github.blindpirate.gogradle.util;


import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class StringUtils {
    public static String join(List<?> list, String seperator) {
        return org.apache.commons.lang3.StringUtils.join(list, seperator);
    }

    public static String removeEnd(String s, String suffix) {
        return org.apache.commons.lang3.StringUtils.removeEnd(s, suffix);
    }

    public static String trimToNull(String s) {
        return org.apache.commons.lang3.StringUtils.trimToNull(s);
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


    public static List<String> splitToLines(String s) {
        if ("".equals(s)) {
            return Collections.emptyList();
        }
        return Arrays.asList(s.split("\n"));
    }

    public static boolean isEmpty(String s) {
        return org.apache.commons.lang3.StringUtils.isEmpty(s);
    }

    public static boolean fileNameStartsWithAny(File file, String... prefix) {
        return Stream.of(prefix).anyMatch(file.getName()::startsWith);
    }

    public static boolean fileNameEndsWithAny(File file, String... suffix) {
        return Stream.of(suffix).anyMatch(file.getName()::endsWith);
    }

    public static boolean fileNameEqualsAny(File file, String... name) {
        return Stream.of(name).anyMatch(file.getName()::equals);
    }
}
