package com.github.blindpirate.gogradle.build

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.WithResource
import com.github.blindpirate.gogradle.core.MockInjectorSupport
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import com.github.blindpirate.gogradle.util.ProcessUtils.ProcessUtilsDelegate
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Files
import java.nio.file.Path

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class DefaultBuildManagerTest extends MockInjectorSupport {
    DefaultBuildManager manager

    File resource
    @Mock
    Project project
    @Mock
    GoBinaryManager binaryManager
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    GitDependencyManager gitDependencyManager
    @Mock
    ProcessUtilsDelegate delegate
    @Mock
    Process process

    GolangPluginSetting setting = new GolangPluginSetting()

    @Before
    void setUp() {
        manager = new DefaultBuildManager(project, binaryManager, setting)
        when(project.getRootDir()).thenReturn(resource)
        setting.packagePath = 'root/package'

        when(binaryManager.getBinaryPath()).thenReturn('go')

        ReflectionUtils.setStaticFinalField(ProcessUtils, 'DELEGATE', delegate)
        when(delegate.run(anyList(), anyMap(), any(File))).thenReturn(process)
    }

    @After
    void cleanUp() {
        ReflectionUtils.setStaticFinalField(ProcessUtils, 'DELEGATE', new ProcessUtils.ProcessUtilsDelegate())
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if .vendor exists before build'() {
        IOUtils.mkdir(resource, '.vendor')
        manager.ensureDotVendorDirNotExist()
    }

    @Test
    void 'symbolic links should be created properly in preparation'() {
        // when
        manager.prepareSymbolicLinks()
        // then
        assertSymbolicLinkLinkToTarget('.gogradle/project_gopath/src/root/package', '.')
    }

    void assertSymbolicLinkLinkToTarget(String link, String target) {
        Path linkPath = resource.toPath().resolve(link)
        Path targetPath = resource.toPath().resolve(target)
        Path relativePathOfLink = Files.readSymbolicLink(linkPath)
        assert linkPath.getParent().resolve(relativePathOfLink).normalize() == targetPath.normalize()
    }

    @Test
    void 'vendor dir should be renamed to .vendor during build'() {
        // given
        IOUtils.mkdir(resource, 'vendor')
        String dirDuringBuild = null
        when(delegate.run(anyList(), anyMap(), any(File))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                dirDuringBuild = IOUtils.safeList(resource).first()
                return process
            }
        })
        // when
        manager.build()
        // then
        assert dirDuringBuild == '.vendor'
        assert new File(resource, 'vendor').exists()
    }

    @Test
    void 'build should succeed'() {
        // given
        setting.extraBuildArgs = ['a', 'b']
        // when
        manager.build()
        // then
        String expectedOutputPath = new File(resource, ".gogradle/${Os.getHostOs()}_${Arch.getHostArch()}_package")
        verify(delegate).run(['go', 'build', '-o', expectedOutputPath, 'a', 'b'],
                [GOPATH: getBuildGopath()],
                resource
        )
    }

    String getBuildGopath() {
        return "" + new File(resource, '.gogradle/project_gopath') + File.pathSeparator + new File(resource, '.gogradle/build_gopath')
    }

    @Test
    void 'test should succeed'() {
        // given
        setting.extraTestArgs = ['a', 'b']
        // when
        manager.test()
        // then
        verify(delegate).run(['go', 'test', 'a', 'b'],
                [GOPATH: getTestGopath()],
                resource
        )
    }

    String getTestGopath() {
        return "" + new File(resource, '.gogradle/project_gopath') +
                File.pathSeparator + new File(resource, '.gogradle/build_gopath') +
                File.pathSeparator + new File(resource, '.gogradle/test_gopath')

    }

    @Test
    void 'settings should take effect'() {
        // given
        setting.targetPlatform = 'windows-amd64, linux-amd64, linux-386'
        setting.outputPattern = 'myresult_${os}_${arch}'
        setting.outputLocation = resource.absolutePath
        // when
        manager.build()
        // then
        assertOutputFile('myresult_windows_amd64')
        assertOutputFile('myresult_linux_amd64')
        assertOutputFile('myresult_linux_386')
    }

    void assertOutputFile(String fileName) {
        verify(delegate).run(['go', 'build', '-o', new File(resource, fileName).toString()],
                [GOPATH: getBuildGopath()],
                resource
        )
    }

    @Test
    void 'relative path should take effect'() {
        // given
        setting.outputLocation = './a/b/c'
        // when
        manager.build()
        // then
        String expectedOutput = "a/b/c/${Os.getHostOs()}_${Arch.getHostArch()}_package"
        verify(delegate).run(['go', 'build', '-o', new File(resource, expectedOutput).toString()],
                [GOPATH: getBuildGopath()],
                resource
        )
    }

    @Test
    void 'build stdout and stderr should be redirected to current process'() {
        // given
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream('stdout'.getBytes(DEFAULT_CHARSET)))
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream('stderr'.getBytes(DEFAULT_CHARSET)))
        Logger mockLogger = mock(Logger)
        ReflectionUtils.setStaticFinalField(DefaultBuildManager, 'LOGGER', mockLogger)
        // when
        manager.build()
        // then
        verify(mockLogger).quiet('stdout')
        verify(mockLogger).error('stderr')
        ReflectionUtils.setStaticFinalField(DefaultBuildManager, 'LOGGER', Logging.getLogger(DefaultBuildManager))
    }

    @Test
    void 'copying a dependency from global cache to project cache should succeed'() {
        // given
        when(resolvedDependency.getName()).thenReturn('root/package')
        // when
        manager.installDependency(resolvedDependency, Configuration.BUILD)
        // then
        Path gopath = resource.toPath().resolve('.gogradle/build_gopath/src/root/package')
        assert gopath.toFile().exists()
        verify(resolvedDependency).installTo(gopath.toFile())
    }

    @Test
    void 'target directory should be cleared before installing'() {
        // given
        when(resolvedDependency.getName()).thenReturn('root/package')
        IOUtils.write(resource, '.gogradle/build_gopath/src/root/package/oldbuildremains.go', '')
        // when
        manager.installDependency(resolvedDependency, Configuration.BUILD)
        // then
        assert !resource.toPath().resolve('.gogradle/build_gopath/src/root/package/oldbuildremains.go').toFile().exists()
    }
}
