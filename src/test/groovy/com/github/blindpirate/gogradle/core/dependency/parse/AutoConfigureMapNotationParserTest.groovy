package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver
import org.junit.Test

class AutoConfigureMapNotationParserTest {

    static class WithoutDefaultConstructor extends AbstractNotationDependency {
        private static final long serialVersionUID = 1

        private WithoutDefaultConstructor() {
        }

        @Override
        Class<? extends DependencyResolver> getResolverClass() {
            return null
        }

        @Override
        boolean isConcrete() {
            return false
        }
    }

    static class WithDefaultConstructor extends AbstractNotationDependency {
        private static final long serialVersionUID = 1

        @Override
        Class<? extends DependencyResolver> getResolverClass() {
            return null
        }

        @Override
        boolean isConcrete() {
            return false
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
