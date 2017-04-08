package com.github.blindpirate.gogradle.core

import org.junit.Test

class GolangConfigurationManagerTest {

    GolangConfigurationManager manager = new GolangConfigurationManager()

    @Test
    void 'getByName should succeed'() {
        assert manager.getByName('build').name == 'build'
        assert manager.getByName('test').name == 'test'
        assert manager.getByName('unexistent') == null
    }
}
