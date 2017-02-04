package com.github.blindpirate.gogradle.support

import org.junit.runners.model.FrameworkMethod

class AccessWebProcessor extends GogradleRunnerProcessor<AccessWeb> {

    @Override
    boolean shouldIgnore(FrameworkMethod method, AccessWeb annotation) {
        return System.getProperty('TEST_ARE_OFFLINE') != null
    }
}
