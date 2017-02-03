package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.dependency.AbstractGolangDependency
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstallFileFilter
import com.github.blindpirate.gogradle.core.mode.BuildMode
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import org.junit.Test

import static com.github.blindpirate.gogradle.util.ReflectionUtils.callStaticMethod

class UselessEnumTest {
    @Test
    void 'values() and valueOf of all enums should succeed'() {
        [BuildMode,
         Os,
         Arch,
         AbstractGolangDependency.NoTransitiveSpec,
         DependencyInstallFileFilter,
         GolangDependency.Namer,
         GogradleGlobal].each(this.&doUselessTest)
    }

    void doUselessTest(Class<? extends Enum> enumClass) {
        callStaticMethod(enumClass, 'values').each {
            Object value = callStaticMethod(enumClass, 'valueOf', it.name())
            assert it.is(value)
        }
    }
}
