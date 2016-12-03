package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.util.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.internal.ProjectBuilderImpl
import org.junit.After
import org.junit.Before
import org.junit.Test

class GolangPluginTest {
    File tmpProjectFolder
    File tmpUserhomeFolder

    private tmpDirectory() {
        File ret = new File("build/tmp/" + UUID.randomUUID());
        ret.mkdir()
        ret;
    }

    @Before
    public void setUp() {
        tmpProjectFolder = tmpDirectory()
        tmpUserhomeFolder = tmpDirectory()
    }

    @After
    public void cleanUp() {
        FileUtils.forceDelete(tmpProjectFolder)
        FileUtils.forceDelete(tmpUserhomeFolder)
    }

    @Test
    public void 'smoke test should success'() {
        Project project = new ProjectBuilderImpl()
                .createProject("test", tmpProjectFolder, tmpUserhomeFolder);
        project.getPluginManager().apply(GolangPlugin)
    }

}