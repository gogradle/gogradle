package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.crossplatform.Os
import org.junit.runners.model.FrameworkMethod

import java.lang.annotation.Annotation

class OnlyOnWindowsProcessor extends GogradleRunnerProcessor<OnlyOnWindows> {
    @Override
    boolean shouldIgnore(FrameworkMethod method, OnlyOnWindows annotation) {
        return Os.getHostOs() != Os.WINDOWS
    }
}
