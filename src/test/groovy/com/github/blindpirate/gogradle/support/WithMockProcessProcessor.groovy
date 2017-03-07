package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.util.ProcessUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

import static com.github.blindpirate.gogradle.util.ProcessUtils.ProcessUtilsDelegate
import static org.mockito.Mockito.mock

class WithMockProcessProcessor extends GogradleRunnerProcessor<WithMockProcess> {

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithMockProcess annotation) {
        ProcessUtilsDelegate mockDelegate = mock(ProcessUtilsDelegate)
        ReflectionUtils.setStaticFinalField(ProcessUtils, 'DELEGATE', mockDelegate)
        ReflectionUtils.setField(instance, 'delegate', mockDelegate)
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithMockProcess annotation) {
        ReflectionUtils.setStaticFinalField(ProcessUtils, 'DELEGATE', new ProcessUtilsDelegate())
    }
}
