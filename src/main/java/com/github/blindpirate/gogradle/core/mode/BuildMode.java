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

package com.github.blindpirate.gogradle.core.mode;

import javax.annotation.Nonnull;
import java.util.Optional;

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;

import static java.text.MessageFormat.format;
import static java.util.Arrays.stream;


public enum BuildMode {
    DEVELOP("DEV") {
        @Override
        public GolangDependencySet determine(GolangDependencySet declaredDependencies,
                                             GolangDependencySet lockedDependencies)
        {
            return declaredDependencies;
        }
    },
    REPRODUCIBLE("REP") {
        @Override
        public GolangDependencySet determine(GolangDependencySet declaredDependencies,
                                             GolangDependencySet lockedDependencies)
        {
            return lockedDependencies;
        }
    };

    private final String abbr;

    BuildMode(@Nonnull String abbr) {
        this.abbr = abbr;
    }

    public String getAbbr() {
        return abbr;
    }

    public abstract GolangDependencySet determine(GolangDependencySet declaredDependencies,
                                                  GolangDependencySet lockedDependencies);

    /**
     * Convert a string to an instance of {@link BuildMode}.
     *
     * @param string the string to be converted
     * @return the instance of {@link BuildMode}
     * @throws IllegalArgumentException if the string cannot be converted
     */
    @Nonnull
    public static BuildMode fromString(@Nonnull String string) {
        final String proposed = string.toUpperCase();
        final Optional<BuildMode> bm = stream(values()).filter(v -> v.name().equals(proposed)
                                                                    || v.abbr.equals(proposed))
                                                       .findAny();

        if (bm.isPresent()) {
            return bm.get();
        } else {
            throw new IllegalArgumentException(format("Unable to convert {} to an instance of BuildMode", string));
        }
    }
}
