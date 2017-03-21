package com.github.blindpirate.gogradle.core

import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Test

class GolangConfigurationTest {
    GolangConfiguration configuration = new GolangConfiguration('build')

    @Test
    void 'explicit exception should be thrown when invoking unsupported methods'() {
        ReflectionUtils.testUnsupportedMethods(configuration, GolangConfiguration,
                ['getDependencyRegistry', 'getName', 'getDependencies', 'getDescription', 'getAllDependencies',
                 'getArtifacts', 'getAllArtifacts','setCanBeConsumed','isCanBeConsumed','setCanBeResolved','isCanBeResolved'])
    }

    @Test
    void 'get description should succeed'() {
        assert configuration.getDescription() == 'build configuration'
    }

    @Test
    void 'hacker method invocation should succeed'() {
        assert configuration.getAllDependencies().isEmpty()

        [configuration.getArtifacts(), configuration.getAllArtifacts()].each {
            assert it.getFiles().isEmpty()
            assert it.getBuildDependencies() == null
        }

    }

}
