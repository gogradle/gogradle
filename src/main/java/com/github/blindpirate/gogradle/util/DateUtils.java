/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
