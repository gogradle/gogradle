package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.core.cache.CacheScope
import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency
import com.github.blindpirate.gogradle.core.dependency.ResolveContext
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import org.junit.Test

class AutoConfigureMapNotationParserTest {

    static class WithoutDefaultConstructor extends AbstractNotationDependency {
        private static final long serialVersionUID = 1

        private WithoutDefaultConstructor() {
        }

        @Override
        protected ResolvedDependency doResolve(ResolveContext context) {
            return null
        }

        @Override
        CacheScope getCacheScope() {
            return null
        }
    }

    static class WithDefaultConstructor extends AbstractNotationDependency {
        private static final long serialVersionUID = 1

        @Override
        protected ResolvedDependency doResolve(ResolveContext context) {
            return null
        }

        @Override
        CacheScope getCacheScope() {
            return null
        }
    }

    @Test(expected = IllegalStateException)
    void 'implementation class should provide a class with default constructor'() {
        new AutoConfigureMapNotationParser<WithoutDefaultConstructor>() {
        }.parse([:])
    }

    @Test
    void ''() {
        assert new AutoConfigureMapNotationParser<WithDefaultConstructor>() {
        }.parse([:]) instanceof WithDefaultConstructor
    }
}
