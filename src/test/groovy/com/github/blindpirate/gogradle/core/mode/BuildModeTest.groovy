package com.github.blindpirate.gogradle.core.mode

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.*
import com.github.blindpirate.gogradle.util.DependencyUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(GogradleRunner)
class BuildModeTest {
    GolangDependencySet declared = GolangDependencySet.empty()
    GolangDependencySet locked = GolangDependencySet.empty()
    GolangDependencySet vendor = GolangDependencySet.empty()
    @Mock
    GogradleRootProject gogradleRootProject

    VendorResolvedDependency a = new VendorResolvedDependencyForTest('a', 'a', 1L, gogradleRootProject, 'vendor/a')
    VendorResolvedDependency b = new VendorResolvedDependencyForTest('b', 'b', 1L, gogradleRootProject, 'vendor/a/vendor/b')
    VendorResolvedDependency cInVendor = new VendorResolvedDependencyForTest('c', 'c', 1L, gogradleRootProject, 'vendor/a/vendor/b/vendor/c')
    NotationDependency cInBuildDotGradle = DependencyUtils.mockWithName(NotationDependency, 'c')
    NotationDependency lockedC = DependencyUtils.mockWithName(NotationDependency, 'c')

    @Before
    void setUp() {
        a.dependencies.add(b)
        b.dependencies.add(cInVendor)

        vendor.add(a)
        declared.add(cInBuildDotGradle)
        locked.add(lockedC)
    }

    @Test
    void 'declared > locked > vendor in DEVELOP mode'() {
        // when
        GolangDependencySet result = BuildMode.DEVELOP.determine(declared, vendor, locked)
        // then
        assert result.size() == 2
        assert result.any { it.is(a) }
        assert result.any { it.is(cInBuildDotGradle) }
        assert a.dependencies.contains(b)
        assert b.dependencies.empty
    }

    @Test
    void 'vendor > locked > declared in REPRODUCIBLE mode'() {
        // when
        GolangDependencySet result = BuildMode.REPRODUCIBLE.determine(declared, vendor, locked)
        // then
        assert result.size() == 1
        assert result.any { it.is(a) }
        assert a.dependencies.contains(b)
        assert b.dependencies.any {it.is(cInVendor)}
    }
}
