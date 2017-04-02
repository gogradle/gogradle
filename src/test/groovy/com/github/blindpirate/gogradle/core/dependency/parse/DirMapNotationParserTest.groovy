package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource('')
class DirMapNotationParserTest {

    DirMapNotationParser parser = new DirMapNotationParser()
    File resource

    @Test
    void 'notation with dir should be parsed correctly'() {
        // when
        GolangDependency dependency = parser.parse([name: 'path', dir: resource.absolutePath])

        // then
        assert dependency instanceof LocalDirectoryDependency
        assert dependency.name == 'path'
        assert dependency.rootDir == resource
    }
}
