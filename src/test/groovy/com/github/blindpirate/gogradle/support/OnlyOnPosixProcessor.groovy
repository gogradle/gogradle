package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.crossplatform.Os
import org.junit.runners.model.FrameworkMethod

class OnlyOnPosixProcessor extends GogradleRunnerProcessor<OnlyOnPosix> {
    @Override
    boolean shouldIgnore(FrameworkMethod method, OnlyOnPosix annotation) {
        return Os.getHostOs() == Os.WINDOWS
    }
}
