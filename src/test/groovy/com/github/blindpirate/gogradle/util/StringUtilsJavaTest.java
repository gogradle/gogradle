package com.github.blindpirate.gogradle.util;

import org.junit.Test;

import static com.github.blindpirate.gogradle.util.StringUtils.eachSubPath;
import static com.github.blindpirate.gogradle.util.StringUtils.eachSubPathReverse;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringUtilsJavaTest {
    @Test
    public void eachSubPathReverse_should_be_correct() {
        assertEquals(eachSubPathReverse("a/bb/ccc").collect(toList()), asList("a/bb/ccc", "a/bb", "a"));
        assertTrue(eachSubPathReverse("a/bb/ccc").anyMatch(s -> s.endsWith("a")));
        assertTrue(eachSubPathReverse("a/bb/ccc").noneMatch(s -> s.endsWith("d")));
        assertEquals(eachSubPathReverse("a/a/c").filter(s -> s.endsWith("a")).findFirst().get(), "a/a");
    }

    @Test
    public void eachSubPath_should_be_correct() {
        assertEquals(eachSubPath("a/bb/ccc").collect(toList()), asList("a", "a/bb", "a/bb/ccc"));
        assertTrue(eachSubPath("a/bb/ccc").anyMatch(s -> s.endsWith("a")));
        assertTrue(eachSubPath("a/bb/ccc").noneMatch(s -> s.endsWith("d")));
        assertEquals(eachSubPath("a/ba/c").filter(s -> s.endsWith("a")).findFirst().get(), "a");
    }
}
