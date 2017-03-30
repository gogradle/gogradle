package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

import static com.github.blindpirate.gogradle.GogradleRunner.tmpRandomDirectory
import static com.github.blindpirate.gogradle.util.IOUtils.deleteQuitely

class WithIsolatedUserhomeProcessor extends GogradleRunnerProcessor<WithIsolatedUserhome> {
    File userhome

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithIsolatedUserhome annotation) {
        userhome=tmpRandomDirectory('userhome')
        ReflectionUtils.setFieldSafely(instance, 'userhome', userhome)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithIsolatedUserhome annotation) {
        deleteQuitely(userhome)
    }
}
