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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class CollectionUtils {

    public static <T> boolean isEmpty(Collection<T> c) {
        return c == null || c.isEmpty();
    }

    public static <T> List<T> immutableList(T... elements) {
        return Collections.unmodifiableList(asList(elements));
    }

    public static <T> List<T> collectOptional(Optional<T>... optionals) {
        List<T> ret = new ArrayList<>();
        for (Optional<T> optional : optionals) {
            optional.ifPresent(ret::add);
        }
        return ret;
    }

    public static List<String> asStringList(Object... elements) {
        List<String> ret = new ArrayList<>();
        Stream.of(elements).forEach(element -> {
            if (element instanceof Collection) {
                ret.addAll((Collection) element);
            } else if (element != null && element.getClass().isArray()) {
                Stream.of((String[]) element).forEach(ret::add);
            } else {
                ret.add((String) element);
            }
        });
        return ret;
    }

    public static <T> List<T> flatten(List<List<T>> lists) {
        return lists.stream().collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }
}
