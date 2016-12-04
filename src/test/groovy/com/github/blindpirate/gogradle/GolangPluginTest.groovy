package com.github.blindpirate.gogradle

import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithProject
class GolangPluginTest {

    Project project

    private Instantiator getInstantiator() {
        return project.services.get(Instantiator);
    }

    @Test
    public void 'smoke test should success'() {
        project.pluginManager.apply(GolangPlugin)
    }

    @Test
    public void 'build and test should be added to configurations'() {
        project.pluginManager.apply(GolangPlugin)
        assert project.configurations.build
        assert project.configurations.test
    }

    @Test
    public void 'add a dependency to configuration should success'() {
        project.pluginManager.apply(GolangPlugin)
        project.dependencies {
            build 'github.com'
        }

        assert project.configurations.build.dependencies.size() == 1
    }

}