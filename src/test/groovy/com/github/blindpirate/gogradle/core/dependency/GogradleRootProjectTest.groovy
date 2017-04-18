package com.github.blindpirate.gogradle.core.dependency

import org.junit.Test

class GogradleRootProjectTest {
    GogradleRootProject rootProject = new GogradleRootProject()

    @Test(expected = IllegalStateException)
    void 'exception should be thrown in second initialization'() {
        rootProject.name = 'name'
        rootProject.initSingleton('name', null)
    }

    @Test
    void 'some methods should throw UnsupportOperationException'() {
        assertUnsupport { rootProject.setDir('') }
        assertUnsupport { rootProject.getUpdateTime() }
        assertUnsupport { rootProject.formatVersion() }
        assertUnsupport { rootProject.getVersion() }
        assertUnsupport { rootProject.clone() }
    }

    @Test
    void 'it should be resolved to itself'() {
        assert rootProject.resolve(null).is(rootProject)
    }

    @Test
    void 'toNotation should succeed'() {
        assert rootProject.toLockedNotation() == [name: 'GOGRADLE_ROOT']
    }

    @Test
    void 'equals and hashCode should succeed'() {
        assert rootProject != new GogradleRootProject()
        assert rootProject.hashCode() != new GogradleRootProject().hashCode()
    }

    void assertUnsupport(Closure c) {
        try {
            c.call()
        }
        catch (Exception e) {
            return
        }
        assert false
    }
}
