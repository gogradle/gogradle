/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle;

import javax.annotation.Nonnull;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static java.util.Arrays.stream;


/**
 * A {@link CacheTimeUnit} represents time durations at a given unit of
 * granularity and provides a utility method to convert a duration into
 * seconds.
 */
public enum CacheTimeUnit {

    SECONDS(java.util.concurrent.TimeUnit.SECONDS),
    MINUTES(java.util.concurrent.TimeUnit.MINUTES),
    HOURS(java.util.concurrent.TimeUnit.HOURS),
    DAYS(java.util.concurrent.TimeUnit.DAYS);

    private final java.util.concurrent.TimeUnit timeUnit;

    CacheTimeUnit(@Nonnull java.util.concurrent.TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * Convert a duration in the units of the enum to seconds.
     *
     * @param duration the duration to be converted
     * @return the converted duration in seconds
     */
    public long toSeconds(long duration) {
        return timeUnit.toSeconds(duration);
    }

    /**
     * Convert a string to an instance of {@link CacheTimeUnit}.
     *
     * @param string the string to be converted
     * @return the instance of {@link CacheTimeUnit}
     * @throws IllegalArgumentException if the string cannot be converted
     */
    @Nonnull
    public static CacheTimeUnit fromString(@Nonnull String string) {
        final String proposed = string.toUpperCase();
        final Optional<CacheTimeUnit> ctu = stream(values()).filter(v -> {
            final String name = v.name();
            return name.equals(proposed) || name.substring(0, name.length() - 1).equals(proposed);
        }).findAny();

        if (ctu.isPresent()) {
            return ctu.get();
        } else {
            throw new IllegalArgumentException(format("Unable to convert {} to an instance of CacheTimeUnit", string));
        }
    }
}
