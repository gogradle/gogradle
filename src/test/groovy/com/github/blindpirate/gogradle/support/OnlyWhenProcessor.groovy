package com.github.blindpirate.gogradle.support

import org.junit.runners.model.FrameworkMethod

class OnlyWhenProcessor extends GogradleRunnerProcessor<OnlyWhen> {
    @Override
    boolean shouldIgnore(FrameworkMethod method, OnlyWhen annotation) {
        return !new GroovyShell().evaluate(annotation.value())
    }
}
