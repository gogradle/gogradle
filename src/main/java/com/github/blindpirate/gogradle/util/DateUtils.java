package com.github.blindpirate.gogradle.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static long toMilliseconds(long second) {
        return 1000L * second;
    }

    public static long toMilliseconds(double second) {
        return (long) (1000L * second);
    }

    public static String formatNow(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }

    public static long parseRaw(String raw) {
        // git and hg raw time
        // 1481274259 +0800
        String[] unixSecondAndTimezone = raw.split(" ");
        Assert.isTrue(unixSecondAndTimezone.length == 2, "Illegal raw time: " + raw);
        return toMilliseconds(Long.parseLong(unixSecondAndTimezone[0]));
    }
}
