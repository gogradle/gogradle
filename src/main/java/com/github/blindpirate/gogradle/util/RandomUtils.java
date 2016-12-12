package com.github.blindpirate.gogradle.util;

public class RandomUtils {
    private static final int PRINTABLE_CHAR_START = 33;
    private static final int PRINTABLE_CHAR_END = 128;

    public static String randomString(int maxLength) {
        int length = org.apache.commons.lang3.RandomUtils.nextInt(0, maxLength + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            sb.append((char) org.apache.commons.lang3.RandomUtils.nextInt(
                    PRINTABLE_CHAR_START,
                    PRINTABLE_CHAR_END));
        }
        return sb.toString();
    }
}
