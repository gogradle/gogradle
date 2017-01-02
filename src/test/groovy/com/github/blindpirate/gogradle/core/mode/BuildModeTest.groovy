package com.github.blindpirate.gogradle.core.mode

import org.junit.Test

class BuildModeTest {
    @Test
    void 'useless test'() {
        assert BuildMode.values().length == 2
        BuildMode.valueOf('Develop') == BuildMode.Develop
    }
}
