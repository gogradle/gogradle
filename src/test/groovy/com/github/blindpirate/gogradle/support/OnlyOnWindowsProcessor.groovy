package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.crossplatform.Os
import org.junit.runners.model.FrameworkMethod

class OnlyOnWindowsProcessor extends GogradleRunnerProcessor {
    @Override
    boolean shouldIgnore(FrameworkMethod method) {
        return Os.getHostOs() != Os.WINDOWS
    }
}
