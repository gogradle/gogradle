/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.*
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.mockito.MockitoAnnotations

import java.lang.annotation.Annotation

/**
 * Check annotations on a test and do some staff if necessary.
 */
class GogradleRunner extends BlockJUnit4ClassRunner {

    private Map annoToProcessorMap = [
            (AccessWeb)              : AccessWebProcessor,
            (MockOffline)            : MockOfflineProcessor,
            (MockRefreshDependencies): MockRefreshDependenciesProcessor,
            (WithProject)            : WithProjectProcessor,
            (WithResource)           : WithResourceProcessor,
            (WithMockInjector)       : WithMockInjectorProcessor,
            (WithMockGo)             : WithMockGoProcessor,
            (WithGitRepos)           : WithGitReposProcessor,
            (WithGitRepo)            : WithGitRepoProcessor,
            (WithIsolatedUserhome)   : WithIsolatedUserhomeProcessor,
            (OnlyWhen)               : OnlyWhenProcessor,
            (OnlyOnPosix)            : OnlyOnPosixProcessor,
            (OnlyOnWindows)          : OnlyOnWindowsProcessor
    ]

    private List<AnnotationAndProcessor> processors

    Object testInstance

    FrameworkMethod testMethod

    GogradleRunner(Class<?> klass) throws InitializationError {
        super(klass)
    }

    @Override
    Object createTest() throws Exception {
        testInstance = super.createTest()
        MockitoAnnotations.initMocks(testInstance)
        processors.each { it.processor.beforeTest(testInstance, testMethod, it.annotation) }
        return testInstance
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        testMethod = method

        processors = annoToProcessorMap.entrySet().findResults { entry ->
            def anno = findAnno(method, entry.key)
            if (anno) {
                return new AnnotationAndProcessor(annotation: anno, processor: entry.value.newInstance())
            } else {
                return null
            }
        }

        if (processors.any { it.processor.shouldIgnore(method, it.annotation) }) {
            notifier.fireTestIgnored(describeChild(method))
            return
        }

        try {
            super.runChild(method, notifier)
        } finally {
            processors.each { it.processor.afterTest(testInstance, method, it.annotation) }
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

    static class AnnotationAndProcessor {
        Annotation annotation
        GogradleRunnerProcessor processor
    }
}
