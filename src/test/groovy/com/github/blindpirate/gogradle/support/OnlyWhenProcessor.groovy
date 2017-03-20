package com.github.blindpirate.gogradle.support

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.runners.model.FrameworkMethod

class OnlyWhenProcessor extends GogradleRunnerProcessor<OnlyWhen> {
    private static final Logger LOGGER = Logging.getLogger(OnlyWhenProcessor.class)

    @Override
    boolean shouldIgnore(FrameworkMethod method, OnlyWhen annotation) {
        try {
            Object scriptRetValue = new GroovyShell().evaluate(annotation.value())
            LOGGER.error("script {} result {}", annotation.value(), scriptRetValue)
            return !scriptRetValue
        } catch (Exception e) {
            LOGGER.error("", e)
            switch (annotation.ignoreTestWhenException()) {
                case OnlyWhen.ExceptionStrategy.TRUE:
                    return true
                case OnlyWhen.ExceptionStrategy.FALSE:
                    return false
                default:
                    throw e
            }
        }
    }
}
