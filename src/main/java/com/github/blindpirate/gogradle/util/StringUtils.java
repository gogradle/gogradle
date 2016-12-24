package com.github.blindpirate.gogradle.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringUtils {
    public static String join(List<?> list, String seperator) {
        return org.apache.commons.lang3.StringUtils.join(list, seperator);
    }

    public static String trimToNull(String s) {
        return org.apache.commons.lang3.StringUtils.trimToNull(s);
    }


    public static String[] splitAndTrim(String str, String seperator) {
        String[] array = org.apache.commons.lang3.StringUtils.split(str, seperator);
        List<String> result = new ArrayList<>();
        for (String s : array) {
            if (StringUtils.isNotBlank(s)) {
                result.add(s.trim());
            }
        }
        return result.toArray(new String[result.size()]);
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
}
