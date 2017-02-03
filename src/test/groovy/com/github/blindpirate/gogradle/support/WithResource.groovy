package com.github.blindpirate.gogradle.support

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Indicates that the resource with specified path is needed in test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@interface WithResource {
    /**
     * The resource path.
     * A temp empty directory will be used if it's a empty string.
     * @return
     */
    String value()
}
