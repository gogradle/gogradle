package com.github.blindpirate.gogradle.util;

import java.util.function.BinaryOperator;

public class Functions {
    public static <T> BinaryOperator<T> returnFirstArg() {
        return (t1, t2) -> t1;
    }
}
