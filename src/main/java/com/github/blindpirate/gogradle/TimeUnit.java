/*
 * Copyright 2017 the original author or authors.
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

package com.github.blindpirate.gogradle;

/**
 * A {@link java.util.concurrent.TimeUnit} represents time durations at a given
 * unit of granularity and provides a utility method to convert a duration into
 * seconds.
 */
public enum TimeUnit {

    SECONDS(java.util.concurrent.TimeUnit.SECONDS),
    MINUTES(java.util.concurrent.TimeUnit.MINUTES),
    HOURS(java.util.concurrent.TimeUnit.HOURS),
    DAYS(java.util.concurrent.TimeUnit.DAYS);

    private final java.util.concurrent.TimeUnit timeUnit;

    TimeUnit(java.util.concurrent.TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public long toSeconds(long duration) {
        return timeUnit.toSeconds(duration);
    }
}
