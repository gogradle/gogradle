package com.github.blindpirate.gogradle.core

import org.junit.Test

class DefaultBuildConstraintManagerTest {
    DefaultBuildConstraintManager manager = new DefaultBuildConstraintManager()

    @Test
    void 'getting ctx should succeed'() {
        manager.getCtx()
    }
}
