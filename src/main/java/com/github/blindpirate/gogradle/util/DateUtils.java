package com.github.blindpirate.gogradle.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static long toMilliseconds(long second) {
        return 1000L * second;
    }

    public static String formatNow(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }
}
