package com.github.blindpirate.gogradle.support

import org.junit.runners.model.FrameworkMethod

class OnlyWhenProcessor extends GogradleRunnerProcessor<OnlyWhen> {
    @Override
    boolean shouldIgnore(FrameworkMethod method, OnlyWhen annotation) {
        try {
            return !new GroovyShell().evaluate(annotation.value())
        } catch (Exception e) {
            switch (annotation.whenException()) {
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
