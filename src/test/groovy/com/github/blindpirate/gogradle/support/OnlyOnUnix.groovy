package com.github.blindpirate.gogradle.support

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Indicates that a test method needs to access web, which may ignored when offline
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD])
@interface OnlyOnUnix {
}