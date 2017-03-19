package com.github.blindpirate.gogradle.support

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.runners.model.FrameworkMethod

class OnlyWhenProcessor extends GogradleRunnerProcessor<OnlyWhen> {
    private static final Logger LOGGER = Logging.getLogger(OnlyWhenProcessor.class)

    @Override
    boolean shouldIgnore(FrameworkMethod method, OnlyWhen annotation) {
        try {
            Object ret = !new GroovyShell().evaluate(annotation.value())
            LOGGER.info("result: {}", ret)
            return ret
        } catch (Exception e) {
            switch (annotation.ignoreTestWhenException()) {
                case OnlyWhen.ExceptionStrategy.TRUE:
                    LOGGER.info("exception with true strategy")
                    return true
                case OnlyWhen.ExceptionStrategy.FALSE:
                    LOGGER.info("exception with false strategy")
                    return false
                default:
                    LOGGER.info("exception with default strategy")
                    throw e
            }
        }
    }
}
