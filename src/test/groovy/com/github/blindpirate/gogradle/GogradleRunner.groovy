package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.*
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.mockito.MockitoAnnotations

/**
 * Check annotations on a test and do some staff if necessary.
 */
class GogradleRunner extends BlockJUnit4ClassRunner {

    private Map annoToProcessorMap = [
            (AccessWeb)       : AccessWebProcessor,
            (MockOffline)     : MockOfflineProcessor,
            (WithProject)     : WithProjectProcessor,
            (WithResource)    : WithResourceProcessor,
            (WithMockInjector): WithMockInjectorProcessor
    ]

    private List<GogradleRunnerProcessor> processors

    Object testInstance

    FrameworkMethod testMethod

    GogradleRunner(Class<?> klass) throws InitializationError {
        super(klass)
    }

    @Override
    Object createTest() throws Exception {
        testInstance = super.createTest()
        MockitoAnnotations.initMocks(testInstance)
        setOfflineIfNecessary()
        processors.each { it.beforeTest(testInstance, testMethod) }
        return testInstance
    }

    def setOfflineIfNecessary() {
        if (ReflectionUtils.getField(GogradleGlobal.INSTANCE, 'offline') == null) {
            ReflectionUtils.setField(GogradleGlobal.INSTANCE, 'offline', false)
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        testMethod = method

        processors = annoToProcessorMap.entrySet().findResults { entry ->
            def anno = findAnno(method, entry.key)
            if (anno) {
                return entry.value.newInstance()
            } else {
                return null
            }
        }

        if (processors.any { it.shouldIgnore(method) }) {
            notifier.fireTestIgnored(describeChild(method))
            return
        }

        try {
            super.runChild(method, notifier)
        } finally {
            processors.each { it.afterTest(testInstance, method) }
        }
    }

    static findAnno(FrameworkMethod method, Class clazz) {
        def annoOnMethod = findAnnoOnMethod(method, clazz)
        if (annoOnMethod) {
            return annoOnMethod
        }
        return findAnnoOnClass(method, clazz)
    }

    static findAnnoOnMethod(FrameworkMethod method, Class clazz) {
        return method.method.getAnnotation(clazz)
    }

    static findAnnoOnClass(FrameworkMethod method, Class annoClass) {
        Class currentClass = method.method.declaringClass
        while (currentClass) {
            def ret = currentClass.getAnnotation(annoClass)
            if (ret) {
                return ret
            }
            currentClass = currentClass.superclass
        }

        return null
    }

    static File tmpRandomDirectory(String prefix) {
        File ret = new File("build/tmp/${prefix}-${UUID.randomUUID()}").absoluteFile
        ret.mkdir()
        return ret
    }
}
