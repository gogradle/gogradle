package com.github.blindpirate.gogradle.vcs.mercurial

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.support.WithMockInjector
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithMockInjector
class MercurialNotationDependencyTest {
    @Mock
    MercurialDependencyManager manager
    @Mock
    ResolveContext context

    MercurialNotationDependency dependency = new MercurialNotationDependency()

    @Test
    void 'correct resolver should be returned'() {
        // given
        when(GogradleGlobal.getInstance(MercurialDependencyManager)).thenReturn(manager)
        // when
        dependency.doResolve(context)
        // then
        verify(manager).resolve(context, dependency)
    }
}
