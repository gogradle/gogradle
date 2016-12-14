package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
class DirMapNotationParserTest {

    DirMapNotationParser parser = new DirMapNotationParser()

    @Test
    void 'notation without dir should be rejected'() {
        assert !parser.accept([:])
    }

    @Test
    void 'notation with dir should be parsed correctly'() {
        // when
        GolangDependency dependency = parser.parseMap([name: 'name', dir: 'dir'])

        // then
        assert dependency.name == 'name'
        assert dependency.path == 'dir'
    }
}
