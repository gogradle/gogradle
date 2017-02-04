package com.github.blindpirate.gogradle.support

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.runners.model.FrameworkMethod

import static com.github.blindpirate.gogradle.GogradleRunner.tmpRandomDirectory
import static com.github.blindpirate.gogradle.util.IOUtils.deleteQuitely
import static com.github.blindpirate.gogradle.util.IOUtils.forceDelete
import static com.github.blindpirate.gogradle.util.ReflectionUtils.setFieldSafely

// Every time we find a @WithProject, a new temp project folder,a new user home folder and
// a new project testInstance will be created
// At the end of that method, these resources will be destroyed
class WithProjectProcessor extends GogradleRunnerProcessor<WithProject> {
    private static final String PROJECT_FEILD = 'project'
    private static final String PROJECT_DIR_FEILD = 'projectDir'
    private static final String USERHOME_FIELD = 'userhome'
    File projectDir
    File userhomeDir
    Project project

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithProject annotation) {
        setUpProject()
        setFieldSafely(instance, PROJECT_FEILD, project)
        setFieldSafely(instance, PROJECT_DIR_FEILD, projectDir)
        setFieldSafely(instance, USERHOME_FIELD, userhomeDir)
    }

    void setUpProject() {
        projectDir = tmpRandomDirectory('project')
        userhomeDir = tmpRandomDirectory('userhome')
        project = ProjectBuilder.builder()
                .withGradleUserHomeDir(userhomeDir)
                .withProjectDir(projectDir)
                .withName('test')
                .build()
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithProject annotation) {
        deleteQuitely(projectDir)
        deleteQuitely(userhomeDir)
    }
}
