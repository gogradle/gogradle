package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency
import com.github.blindpirate.gogradle.support.WithResource
import org.junit.Test
import org.junit.runner.RunWith

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString

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

    @Test
    void 'parsing dependency with LocalDirectoryGolangPackage should succeed'() {
        // when
        GolangDependency result = parser.parse([name: 'local', package: LocalDirectoryGolangPackage.of('local', 'local', toUnixString(resource))])
        // then
        assert result instanceof LocalDirectoryDependency
        assert result.name == 'local'
        assert result.rootDir == resource
    }
}
