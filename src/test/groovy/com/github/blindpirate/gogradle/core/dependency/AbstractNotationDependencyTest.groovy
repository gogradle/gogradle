package com.github.blindpirate.gogradle.core.dependency

import com.github.blindpirate.gogradle.GogradleGlobal
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.MockUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency.PropertiesExclusionPredicate
import static com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependencyTest.ResolvedDependencyForTest
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
class AbstractNotationDependencyTest {
    File resource

    AbstractNotationDependency dependency = mock(AbstractNotationDependency, CALLS_REAL_METHODS)

    @Before
    void setUp() {
        when(dependency.getResolverClass()).thenReturn(DependencyResolver)
        ReflectionUtils.setField(dependency, 'transitiveDepExclusions', [] as Set)
    }

    @Test
    @WithMockInjector
    void 'resolved result should be cached'() {
        // given
        ResolveContext context = mock(ResolveContext)
        DependencyResolver resolver = mock(DependencyResolver)
        when(resolver.resolve(context, dependency)).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                return mock(ResolvedDependency)
            }
        })
        when(GogradleGlobal.INSTANCE.getInstance(DependencyResolver)).thenReturn(resolver)
        assert dependency.resolve(context).is(dependency.resolve(context))
    }

    @Test
    void 'setting transitive should succeed'() {
        // when
        dependency.setTransitive(false)
        // then
        // exclude any transitive dependencies
        assert dependency.getTransitiveDepExclusions().first().test(null)
    }

    @Test
    void 'exclude some properties should succeed'() {
        // given
        GolangDependency dependency = mock(GolangDependency)
        when(dependency.getName()).thenReturn('a')
        // when
        this.dependency.exclude([name: 'a'])
        // then
        assert this.dependency.getTransitiveDepExclusions().first().test(dependency)
    }

    @Test
    void 'exclude non-name properties should succeed'() {
        // given
        GolangDependency dependency = mock(GolangDependency)
        when(dependency.getVersion()).thenReturn('version')
        // when
        this.dependency.exclude([version: 'version'])
        // then
        assert this.dependency.getTransitiveDepExclusions().first().test(dependency)
    }

    @Test
    void 'setting package should succeed'() {
        def dependency = new NotationDependencyForTest()
        GolangPackage pkg = MockUtils.mockVcsPackage()
        dependency.package = pkg
        assert dependency.package == pkg
    }

    @Test
    void 'multiple PropertiesExclusionSpec should be compared properly'() {
        PropertiesExclusionPredicate spec1 = PropertiesExclusionPredicate.of([name: 'name'])
        PropertiesExclusionPredicate spec2 = PropertiesExclusionPredicate.of([name: 'name'] as TreeMap)
        assert spec1.equals(spec2)
        assert spec1.equals(spec1)
        assert !spec1.equals(null)
        assert spec1.hashCode() == spec2.hashCode()
    }

    @Test
    @WithResource('')
    void 'serialization and deserialization should succeed'() {
        // given
        AbstractNotationDependency dependency = new NotationDependencyForTest()
        dependency.name = 'name'
        dependency.firstLevel = true
        dependency.exclude([name: 'excludedName', version: 'excludedVersion'])
        dependency.transitive = false
        dependency.package = MockUtils.mockVcsPackage()

        ResolvedDependency resolvedDependency = new ResolvedDependencyForTest('name', 'version', 123L, null)
        resolvedDependency.dependencies.add(LocalDirectoryDependency.fromLocal('local', resource))
        ReflectionUtils.setField(dependency, 'resolvedDependency', resolvedDependency)

        // when
        IOUtils.serialize(dependency, new File(resource, 'out.bin'))
        NotationDependencyForTest result = IOUtils.deserialize(new File(resource, 'out.bin'))
        // then
        assert result.name == 'name'
        assert result.firstLevel
        assert result.transitiveDepExclusions.size() == 2
        assert result.transitiveDepExclusions.contains(PropertiesExclusionPredicate.of([name: 'excludedName', version: 'excludedVersion']))
        assert result.transitiveDepExclusions.contains(AbstractNotationDependency.NO_TRANSITIVE_DEP_PREDICATE)
        assert MockUtils.isMockVcsPackage(result.package)

        ResolvedDependencyForTest resolvedDependencyInResult = ReflectionUtils.getField(dependency, 'resolvedDependency')
        assert resolvedDependencyInResult.name == 'name'
        assert resolvedDependencyInResult.version == 'version'
        assert resolvedDependencyInResult.updateTime == 123L
        assert resolvedDependencyInResult.dependencies.size() == 1
        assert resolvedDependencyInResult.dependencies.first() == LocalDirectoryDependency.fromLocal('local', resource)
    }

    static class NotationDependencyForTest extends AbstractNotationDependency {
        private static final long serialVersionUID = 1

        @Override
        protected Class<? extends DependencyResolver> getResolverClass() {
            return null
        }
    }
}
