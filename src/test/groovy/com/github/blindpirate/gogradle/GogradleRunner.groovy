package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.util.FileUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import net.lingala.zip4j.core.ZipFile
import org.gradle.api.Project
import org.gradle.testfixtures.internal.ProjectBuilderImpl
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.mockito.MockitoAnnotations

import java.nio.file.Path
import java.nio.file.Paths

import static com.github.blindpirate.gogradle.util.FileUtils.forceDelete

/**
 * <ul>
 *  <li>1.Check the usage of {@link WithProject} and create {@link Project} currentInstance if necessary.</li>
 *  <li>2.Check the usage of {@link WithResource} and copy (or unzip) resources to temp directory if necessary.</li>
 *  <li>3.Inject the project currentInstance and resource directory into the test class currentInstance.</li>
 *  <li>4.Inject fields annotated with{@link @Mock} and {@link @InjectMocks}.</li>
 *  <li>5.Clean up temp directories whenever necessary.</li>
 * <ul>
 */
// TODO: ignore @AccessWeb when offline
public class GogradleRunner extends BlockJUnit4ClassRunner {

    Object currentInstance

    // Every time we find a @WithProject, a new temp project folder,a new user home folder and
    // a new project currentInstance will
    // be created then pushed into corresponding stack.
    // At the end of that method, these resources will be popped and destroyed
    ArrayDeque<File> tmpProjectDirStack = new ArrayDeque<>()
    ArrayDeque<File> tmpUserhomeDirStack = new ArrayDeque<>()
    ArrayDeque<Project> projectStack = new ArrayDeque<>()

    // Every time we find a @WithResource, that resource will be copyed(or unzipped) to a temp dir
    // At the end of that method, these resource will be popped and destroyed
    ArrayDeque<File> tmpResourceDirStack = new ArrayDeque<>()

    public GogradleRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public Object createTest() throws Exception {
        currentInstance = super.createTest()
        MockitoAnnotations.initMocks(currentInstance)
        injectProjectAndResourceIfNecessary()
        return currentInstance;
    }

    def injectProjectAndResourceIfNecessary() {
        if (!projectStack.isEmpty()) {
            ReflectionUtils.setField(currentInstance, 'project', projectStack.last)
        }

        if (!tmpResourceDirStack.isEmpty()) {
            ReflectionUtils.setField(currentInstance, 'resource', tmpResourceDirStack.last)
        }
    }

    void popResourceFromStack() {
        File tmpResourceDir = tmpResourceDirStack.pop()
        forceDelete(tmpResourceDir)
    }

    Object popProjectFromStack() {
        File tmpProjectDir = tmpProjectDirStack.pop()
        File tmpUserhomeDir = tmpUserhomeDirStack.pop()
        projectStack.pop()

        forceDelete(tmpProjectDir)
        forceDelete(tmpUserhomeDir)
    }

    File pushResourceToStack(String resourceName) {
        File destDir = tmpRandomDirectory("resource");
        // when resource name is empty, the new created empty dir will be used
        if (resourceName.endsWith('zip')) {
            unzipResourceToDir(resourceName, destDir)
        } else if (resourceName != '') {
            copyResourceToDir(resourceName, destDir)
        }

        tmpResourceDirStack.push(destDir)
    }

    def copyResourceToDir(String resourceName, File destDir) {
        Path source = Paths.get(this.class.classLoader.getResource(resourceName).toURI())
        FileUtils.copyDirectory(source.toFile(), destDir)
    }

    def unzipResourceToDir(String resourceName, File destDir) {
        URI uri = this.class.classLoader.getResource(resourceName).toURI()
        ZipFile zipFile = new ZipFile(new File(uri))
        zipFile.extractAll(destDir.toString())
    }

    def pushProjectToStack() {
        File tmpProjectDir = tmpRandomDirectory('project')
        File tmpUserhomeDir = tmpRandomDirectory('userhome')
        Project project = new ProjectBuilderImpl().createProject('test', tmpProjectDir, tmpUserhomeDir)

        tmpProjectDirStack.push(tmpProjectDir)
        tmpUserhomeDirStack.push(tmpUserhomeDir)
        projectStack.push(project)
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        // TODO not valid yet
        if (System.getProperty("TEST_ARE_OFFLINE")
                && findAnnotation(method, AccessWeb)) {
            notifier.fireTestIgnored(description)
            return
        }

        beforeOneTest(method)

        super.runChild(method, notifier);

        afterOneTest(method);
    }

    void afterOneTest(FrameworkMethod method) {
        WithResource withResource = findWithResource(method)
        if (withResource) {
            popResourceFromStack()
        }
        WithProject withProject = findWithProject(method)
        if (withProject) {
            popProjectFromStack()
        }
    }


    def beforeOneTest(FrameworkMethod method) {
        WithResource withResource = findWithResource(method);
        if (withResource) {
            pushResourceToStack(withResource.value())
        }
        WithProject withProject = findWithProject(method)
        if (withProject) {
            pushProjectToStack()
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

    def findAnnoOnClass(FrameworkMethod method, Class clazz) {
        return method.method.declaringClass.getAnnotation(clazz)
    }

    def tmpRandomDirectory(String prefix) {
        File ret = new File("build/tmp/${prefix}-${UUID.randomUUID()}")
        ret.mkdir()
        ret
    }
}
