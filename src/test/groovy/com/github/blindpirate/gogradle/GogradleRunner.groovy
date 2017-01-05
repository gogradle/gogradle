package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import net.lingala.zip4j.core.ZipFile
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.mockito.MockitoAnnotations

import java.nio.file.Path
import java.nio.file.Paths

import static IOUtils.forceDelete
import static com.github.blindpirate.gogradle.util.ReflectionUtils.setFieldSafely

/**
 * <ul>
 *  <li>1.Check the usage of {@link WithProject} and create {@link Project} instance if necessary.</li>
 *  <li>2.Check the usage of {@link WithResource} and copy (or unzip) resources to temp directory if necessary.</li>
 *  <li>3.Inject the project currentInstance and resource directory into the test class currentInstance.</li>
 *  <li>4.Inject fields annotated with{@link @Mock} and {@link @InjectMocks}.</li>
 *  <li>5.Clean up temp directories whenever necessary.</li>
 * <ul>
 */
// TODO: ignore @AccessWeb when offline
class GogradleRunner extends BlockJUnit4ClassRunner {

    private static final String PROJECT_FEILD = 'project'
    private static final String PROJECT_DIR_FEILD = 'projectDir'
    private static final String USERHOME_FIELD = 'userhome'
    private static final String RESOURC_FIELD = 'resource'

    Object currentInstance

    // Every time we find a @WithProject, a new temp project folder,a new user home folder and
    // a new project currentInstance will be created
    // At the end of that method, these resources will be destroyed
    File projectDir
    File userhomeDir
    Project project

    // Every time we find a @WithResource, that resource will be copyed(or unzipped) to a temp dir
    // At the end of that method, these resource will be  destroyed
    File resourceDir

    GogradleRunner(Class<?> klass) throws InitializationError {
        super(klass)
    }

    @Override
    Object createTest() throws Exception {
        currentInstance = super.createTest()
        MockitoAnnotations.initMocks(currentInstance)
        injectProjectAndResourceIfNecessary()
        return currentInstance
    }

    def injectProjectAndResourceIfNecessary() {
        if (project != null) {
            setFieldSafely(currentInstance, PROJECT_FEILD, project)
            setFieldSafely(currentInstance, PROJECT_DIR_FEILD, projectDir)
            setFieldSafely(currentInstance, USERHOME_FIELD, userhomeDir)
        }

        if (resourceDir != null) {
            setFieldSafely(currentInstance, RESOURC_FIELD, resourceDir)
        }
    }



    void cleanUpResource() {
        forceDelete(resourceDir)
        resourceDir = null
    }

    Object cleanUpProject() {
        forceDelete(projectDir)
        forceDelete(userhomeDir)
        projectDir = null
        userhomeDir = null
        project = null
    }

    File setUpResource(String resourceName) {
        File destDir = tmpRandomDirectory("resource")
        // when resource path is empty, the new created empty dir will be used
        if (resourceName.endsWith('zip')) {
            unzipResourceToDir(resourceName, destDir)
        } else if (resourceName != '') {
            copyResourceToDir(resourceName, destDir)
        }

        resourceDir = destDir
    }

    def copyResourceToDir(String resourceName, File destDir) {
        Path source = Paths.get(this.class.classLoader.getResource(resourceName).toURI())
        IOUtils.copyDirectory(source.toFile(), destDir)
    }

    def unzipResourceToDir(String resourceName, File destDir) {
        URI uri = this.class.classLoader.getResource(resourceName).toURI()
        ZipFile zipFile = new ZipFile(new File(uri))
        zipFile.extractAll(destDir.toString())
    }

    def setUpProject() {
        projectDir = tmpRandomDirectory('project')
        userhomeDir = tmpRandomDirectory('userhome')
        project = ProjectBuilder.builder()
                .withGradleUserHomeDir(userhomeDir)
                .withProjectDir(projectDir)
                .withName('test')
                .build()
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (System.getProperty("TEST_ARE_OFFLINE")
                && findAnnoOnMethod(method, AccessWeb)) {
            notifier.fireTestIgnored(describeChild(method))
            return
        }

        beforeOneTest(method)
        try {
            super.runChild(method, notifier)
        } finally {
            afterOneTest(method)
        }
    }

    void afterOneTest(FrameworkMethod method) {
        WithResource withResource = findWithResource(method)
        if (withResource) {
            cleanUpResource()
        }
        WithProject withProject = findWithProject(method)
        if (withProject) {
            cleanUpProject()
        }
    }


    def beforeOneTest(FrameworkMethod method) {
        WithResource withResource = findWithResource(method)
        if (withResource) {
            setUpResource(withResource.value())
        }
        WithProject withProject = findWithProject(method)
        if (withProject) {
            setUpProject()
        }
    }

    WithProject findWithProject(FrameworkMethod method) {
        WithProject annoOnMethod = findAnnoOnMethod(method, WithProject)
        if (annoOnMethod) {
            return annoOnMethod
        }
        return findAnnoOnClass(method, WithProject)
    }

    WithResource findWithResource(FrameworkMethod method) {
        WithResource annoOnMethod = findAnnoOnMethod(method, WithResource)
        if (annoOnMethod) {
            return annoOnMethod
        }
        return findAnnoOnClass(method, WithResource)
    }

    def findAnnoOnMethod(FrameworkMethod method, Class clazz) {
        return method.method.getAnnotation(clazz)
    }

    def findAnnoOnClass(FrameworkMethod method, Class annoClass) {
        Class currentClass = method.method.declaringClass
        while (currentClass) {
            def ret = currentClass.getAnnotation(annoClass)
            if (ret) {
                return ret
            }
            currentClass = currentClass.superclass
        }

        return null
    }

    def tmpRandomDirectory(String prefix) {
        File ret = new File("build/tmp/${prefix}-${UUID.randomUUID()}").absoluteFile
        ret.mkdir()
        ret
    }
}
