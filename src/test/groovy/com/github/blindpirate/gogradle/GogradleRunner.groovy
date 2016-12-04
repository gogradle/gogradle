package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.util.ReflectionUtils
import net.lingala.zip4j.core.ZipFile
import org.gradle.api.Project
import org.gradle.testfixtures.internal.ProjectBuilderImpl
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.junit.runners.model.Statement
import org.mockito.MockitoAnnotations

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static com.github.blindpirate.gogradle.util.FileUtils.forceDelete

public class GogradleRunner extends BlockJUnit4ClassRunner {
    Object instance

    File tmpProjectFolder = tmpRandomDirectory('project')
    File tmpUserhomeFolder = tmpRandomDirectory('userhome')
    File tmpResourceFolder = tmpDirectory('resource')

    public GogradleRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public Object createTest() throws Exception {
        instance = super.createTest();
        MockitoAnnotations.initMocks(instance);
        processWithProjectOnClass(instance);
        processWithResourceOnClass(instance)
        return instance;
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        cleanUp()
        super.classBlock(notifier)
    }

    def processWithResourceOnClass(Object instance) {
        WithResource anno = instance.class.getAnnotation(WithResource)
        if (anno) {
            String resourceName = anno.value();
            if (resourceName.endsWith('zip')) {
                unzipResourceToTmpDir(resourceName)
            } else {
                copyResourceToTmpDir(resourceName)
            }
        }
    }

    def copyResourceToTmpDir(String resourceName) {
        Path source = Paths.get(this.class.classLoader.getResource(resourceName).toURI())
        Path dest = tmpResourceFolder.toPath()
        Files.copy(source, dest)
    }

    def unzipResourceToTmpDir(String resourceName) {
        URI uri = this.class.classLoader.getResource(resourceName).toURI()
        ZipFile zipFile = new ZipFile(new File(uri))
        zipFile.extractAll(tmpResourceFolder.path)
    }


    def processWithProjectOnClass(Object instance) {
        WithProject anno = instance.class.getAnnotation(WithProject);
        if (anno) {
            ReflectionUtils.setField(instance, 'project', newProject());
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        processWithProjectOnMethod(method)
        processWithResourceOnMethod(method)

        super.runChild(method, notifier);
    }

    def processWithResourceOnMethod(FrameworkMethod frameworkMethod) {
        WithResource anno = frameworkMethod.getAnnotation(WithResource);
        if (anno) {
            String resourceName = anno.value();
            if (resourceName.endsWith('zip')) {
                unzipResourceToTmpDir(resourceName)
            } else {
                copyResourceToTmpDir(resourceName)
            }
        }

    }

    def processWithProjectOnMethod(FrameworkMethod frameworkMethod) {
        WithProject anno = frameworkMethod.getAnnotation(WithProject);
        if (anno) {
            ReflectionUtils.setField(instance, 'project', newProject());
        }
    }

    private Project newProject() {
        new ProjectBuilderImpl()
                .createProject("test", tmpProjectFolder, tmpUserhomeFolder);
    }

    def tmpRandomDirectory(String prefix) {
        File ret = new File("build/tmp/${prefix}-${UUID.randomUUID()}");
        ret.mkdir()
        ret
    }

    def tmpDirectory(String name) {
        File ret = new File("build/tmp/${name}")
        ret.mkdir()
        ret
    }


    public void cleanUp() {
        forceDelete(tmpProjectFolder)
        forceDelete(tmpUserhomeFolder)
        forceDelete(tmpResourceFolder)
    }
}
