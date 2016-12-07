package com.github.blindpirate.gogradle.util;


import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static String join(List<?> list, String seperator) {
        return org.apache.commons.lang3.StringUtils.join(list, seperator);
    }

    /**
     * Split and trim. Will remove empty strings
     *
     * @param str
     * @param seperator
     * @return
     */
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

    private static boolean isNotBlank(String s) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(s);
    }
}
