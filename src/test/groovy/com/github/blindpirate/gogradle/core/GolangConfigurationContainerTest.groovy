package com.github.blindpirate.gogradle.core

import org.gradle.internal.reflect.Instantiator
import org.junit.Test
import org.mockito.Mockito

class GolangConfigurationContainerTest {

    Instantiator instantiator = Mockito.mock(Instantiator)

    GolangConfigurationContainer container = new GolangConfigurationContainer(instantiator)

    @Test(expected = UnsupportedOperationException)
    void 'exception should be thrown when invoking unsupported method'() {
        container.detachedConfiguration(null)
    }
}
