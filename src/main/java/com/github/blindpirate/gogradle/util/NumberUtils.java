package com.github.blindpirate.gogradle.util;

public class NumberUtils {
    public static int percentage(Number numerator, Number denominator) {
        return (int) Math.round(100 * numerator.doubleValue() / denominator.doubleValue());
    }
}
