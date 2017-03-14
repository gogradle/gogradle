package com.github.blindpirate.gogradle.util;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

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
