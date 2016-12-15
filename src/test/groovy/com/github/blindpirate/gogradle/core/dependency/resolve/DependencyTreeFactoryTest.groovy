package com.github.blindpirate.gogradle.core.dependency.resolve

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackageModule
import com.github.blindpirate.gogradle.core.dependency.DependencyRegistry
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyTreeNode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock

import static com.github.blindpirate.gogradle.util.DependencyUtils.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class DependencyTreeFactoryTest {
    @InjectMocks
    DependencyTreeFactory dependencyTreeFactory

    @Mock
    DependencyFactory dependencyFactory

    @Mock
    DependencyRegistry dependencyRegistry

    @Mock
    GolangPackageModule _0
    @Mock
    GolangPackageModule _01
    @Mock
    GolangPackageModule _02
    @Mock
    GolangPackageModule _011
    @Mock
    GolangPackageModule _012

    @Before
    void setUp() {
        when(_0.getName()).thenReturn("0")
        when(_01.getName()).thenReturn("01")
        when(_02.getName()).thenReturn("02")
        when(_011.getName()).thenReturn("011")
        when(_012.getName()).thenReturn("012")

        def dependenciesOf0 = asGolangDependencySet(_01, _02)
        def dependenciesOf01 = asGolangDependencySet(_011, _012)
        when(dependencyFactory.produce(_0)).thenReturn(dependenciesOf0)
        when(dependencyFactory.produce(_01)).thenReturn(dependenciesOf01)

        [_02,_011,_012].each {
            when(dependencyFactory.produce(it)).thenReturn(new GolangDependencySet())
        }

        [_0, _01, _02, _011].each {
            when(it.getPackage()).thenReturn(it)
            when(dependencyRegistry.register(it)).thenReturn(true)
        }

        // conflict with existing package
        when(dependencyRegistry.register(_012)).thenReturn(false)
    }

    @Test
    void 'building a tree should success'() {
        // when
        DependencyTreeNode tree = dependencyTreeFactory.getTree(_0)

        // then
        assert tree.children.size() == 2

        def node01 = tree.children.find { it.value.is(_01) }
        assert node01.children.size() == 1
        assert node01.children.any { it.value.is(_011) }

        def node02 = tree.children.find { it.value.is(_02) }
        assert node02.children.size() == 0
    }

}
