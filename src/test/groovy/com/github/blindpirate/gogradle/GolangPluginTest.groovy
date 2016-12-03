package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.util.FileUtils
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.internal.ProjectBuilderImpl
import org.junit.AfterClass
import org.junit.Test

class GolangPluginTest {
    static File tmpProjectFolder = tmpDirectory()
    static File tmpUserhomeFolder = tmpDirectory()
    static Project project = new ProjectBuilderImpl()
            .createProject("test", tmpProjectFolder, tmpUserhomeFolder);

    static private tmpDirectory() {
        File ret = new File("build/tmp/" + UUID.randomUUID());
        ret.mkdir()
        ret;
    }

    @AfterClass
    public static void cleanUp() {
        FileUtils.forceDelete(tmpProjectFolder)
        FileUtils.forceDelete(tmpUserhomeFolder)
    }

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

}