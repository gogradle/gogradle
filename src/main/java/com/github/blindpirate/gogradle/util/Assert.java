package com.github.blindpirate.gogradle.util;

import org.apache.http.util.Asserts;

public class Assert {

    public static void isTrue(boolean absolute, String s) {
        Asserts.check(absolute, s);
    }
}
