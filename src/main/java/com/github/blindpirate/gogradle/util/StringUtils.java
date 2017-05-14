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


import groovy.text.GStringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class StringUtils {
    public static String removeEnd(String s, String suffix) {
        return org.apache.commons.lang3.StringUtils.removeEnd(s, suffix);
    }

    public static String[] splitAndTrim(String str, String regex) {
        String[] array = str.split(regex);
        return Stream.of(array)
                .map(org.apache.commons.lang3.StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

    public static boolean isNotBlank(String s) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(s);
    }

    public static boolean isBlank(String s) {
        return org.apache.commons.lang3.StringUtils.isBlank(s);
    }

    public static boolean allBlank(String... strs) {
        return Arrays.stream(strs).allMatch(StringUtils::isBlank);
    }

    public static boolean isEmpty(String s) {
        return org.apache.commons.lang3.StringUtils.isEmpty(s);
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static boolean fileNameStartsWithAny(File file, String... prefix) {
        return startsWithAny(file.getName(), prefix);
    }

    public static boolean fileNameEndsWithAny(File file, String... prefix) {
        return endsWithAny(file.getName(), prefix);
    }

    public static boolean startsWithAny(String str, String... prefix) {
        return Stream.of(prefix).anyMatch(str::startsWith);
    }

    public static boolean endsWithAny(String str, String... suffix) {
        return Stream.of(suffix).anyMatch(str::endsWith);
    }

    public static boolean fileNameEqualsAny(File file, String... name) {
        return Stream.of(name).anyMatch(file.getName()::equals);
    }

    public static String toUnixString(File file) {
        return toUnixString(file.getAbsolutePath());
    }

    public static boolean isPrefix(String a, String b) {
        Assert.isNotNull(a);
        Assert.isNotNull(b);
        return !a.equals(b) && b.startsWith(a);
    }

    public static String toUnixString(Path path) {
        return path.toString().replace("\\", "/");
    }

    public static String toUnixString(String s) {
        return s.replace("\\", "/");
    }

    public static String render(String template, Map<String, Object> context) {
        try {
            context = new HashMap<>(context);
            return new GStringTemplateEngine().createTemplate(template).make(context).toString();
        } catch (ClassNotFoundException | IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    public static String substring(String s, int start, int end) {
        return org.apache.commons.lang3.StringUtils.substring(s, start, end);
    }

    public static String formatEnv(Map<String, String> env) {
        return String.join("\n",
                env.entrySet().stream().map(entry -> " " + entry.getKey() + "=" + entry.getValue()).collect(toList()));
    }

    public static int lastIndexOf(String s, String substr) {
        return org.apache.commons.lang3.StringUtils.lastIndexOf(s, substr);
    }

    public static String trimToNull(String str) {
        return org.apache.commons.lang3.StringUtils.trimToNull(str);
    }
}
