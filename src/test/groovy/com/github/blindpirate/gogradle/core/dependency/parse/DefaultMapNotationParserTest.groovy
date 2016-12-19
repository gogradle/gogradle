package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.GolangDependency
import com.github.blindpirate.gogradle.core.pack.PackageInfo
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.vcs.Git
import com.github.blindpirate.gogradle.vcs.VcsType
import com.google.inject.Injector
import com.google.inject.Key
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Matchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class DefaultMapNotationParserTest {

    DefaultMapNotationParser parser
    @Mock
    DirMapNotationParser dirMapNotationParser
    @Mock
    PackageNameResolver packageNameResolver
    @Mock
    MapNotationParser vcsMapNotationParser
    @Mock
    GolangDependency dependency
    @Mock
    PackageInfo packageInfo
    @Mock
    Injector injector

    @Before
    void setUp() {
        parser = new DefaultMapNotationParser(dirMapNotationParser, packageNameResolver)
    }

    @Test(expected = Exception)
    void 'notation without name should be rejected'() {
        parser.parse([:])
    }


    @Test
    void 'notation should be delegated to corresponding parser'() {
        // given
        def notation = [name: 'name', dir: 'dir']
        // when
        parser.parse(notation)

        // then
        verify(dirMapNotationParser).parse(notation)
    }

    @Test
    void 'notation should be delegated to vcs parser'() {
        // given
        Map notation = [name: 'name']
        when(packageNameResolver.produce('name')).thenReturn(packageInfo)
        when(packageInfo.getVcsType()).thenReturn(VcsType.Git)
        MockUtils.mockVcsService(injector, MapNotationParser, Git, vcsMapNotationParser)

        // when
        parser.parse(notation)

        // then
        verify(vcsMapNotationParser).parse(eq([name: 'name', 'info': packageInfo]))

    }

}
