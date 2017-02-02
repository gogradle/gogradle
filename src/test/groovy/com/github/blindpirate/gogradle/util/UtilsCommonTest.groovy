package com.github.blindpirate.gogradle.util

import com.google.common.reflect.ClassPath
import org.junit.Test

class UtilsCommonTest {
    @Test
    void 'useless test for invoking constructor of util class'() {
        ClassPath.from(IOUtils.classLoader).getTopLevelClasses('com.github.blindpirate.gogradle.util').each {
            if (!it.name.endsWith('Test')) {
                it.load().newInstance()
            }
        }
    }
}
