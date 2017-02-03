package com.github.blindpirate.gogradle.support

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Creates a {@link org.gradle.api.Project} testInstance and inject it into "project" member of annotated class.
 * If on method, every time the method is invoked, a new {@link org.gradle.api.Project} will be injected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@interface WithProject {
}
