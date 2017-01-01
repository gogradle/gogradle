package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryNotationDependency
import org.junit.Test

class DirMapNotationParserTest {

    DirMapNotationParser parser = new DirMapNotationParser()

    @Test
    void 'notation with dir should be parsed correctly'() {
        // when
        GolangDependency dependency = parser.parse([name: 'path', dir: 'dir'])

        // then
        assert dependency instanceof LocalDirectoryNotationDependency
        assert dependency.name == 'path'
        assert dependency.dir == 'dir'
    }
}
