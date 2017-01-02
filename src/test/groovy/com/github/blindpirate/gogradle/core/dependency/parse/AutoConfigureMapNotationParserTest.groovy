package com.github.blindpirate.gogradle.core.dependency.parse

import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency
import com.github.blindpirate.gogradle.core.dependency.NotationDependency
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver
import org.junit.Test

class AutoConfigureMapNotationParserTest {

    class NotationDependencyWithoutDefaultConstructor extends AbstractNotationDependency {
        private NotationDependencyWithoutDefaultConstructor() {

        }

        @Override
        protected Class<? extends DependencyResolver> resolverClass() {
            return null
        }
    }

    class Test1 extends AutoConfigureMapNotationParser {
        @Override
        protected Class<? extends NotationDependency> determineDependencyClass(Map<String, Object> notationMap) {
            return NotationDependencyWithoutDefaultConstructor
        }
    }

    @Test(expected = IllegalStateException)
    void 'implementation class should provide a class with default constructor'() {
        new Test1().parse([:])
    }
}
