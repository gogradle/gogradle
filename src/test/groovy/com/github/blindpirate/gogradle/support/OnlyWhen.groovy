package com.github.blindpirate.gogradle.support

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@interface OnlyWhen {
    String value()

    ExceptionStrategy ignoreTestWhenException() default ExceptionStrategy.FAIL

    enum ExceptionStrategy {
        /**
         * When exception is thrown, the OnlyWhen will be treated as true
         */
        TRUE,
        /**
         * When exception is thrown, the OnlyWhen will be treated as false
         */
        FALSE,
        /**
         * When exception is thrown, the test will failed
         */
        FAIL
    }
}