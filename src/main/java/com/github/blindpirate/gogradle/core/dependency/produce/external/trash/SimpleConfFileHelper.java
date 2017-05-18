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

package com.github.blindpirate.gogradle.core.dependency.produce.external.trash;

import java.util.Arrays;
import java.util.Map;
import java.util.OptionalInt;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public interface SimpleConfFileHelper {
    Pattern HEX_PATTERN = Pattern.compile("[a-fA-F0-9]+");

    static void determineVersionAndPutIntoMap(Map<String, Object> ret, String version) {
        if (HEX_PATTERN.matcher(version).matches()) {
            ret.put("version", version);
        } else {
            ret.put("tag", version);
        }
    }

    static boolean isNotCommentLine(String line) {
        return !line.trim().startsWith("#");
    }

    static String[] removeComment(String[] array) {
        OptionalInt index = IntStream.range(0, array.length)
                .filter(i -> array[i].startsWith("#"))
                .findFirst();
        return index.isPresent() ? Arrays.copyOfRange(array, 0, index.getAsInt()) : array;
    }
}
