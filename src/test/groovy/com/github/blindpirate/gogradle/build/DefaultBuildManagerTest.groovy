package com.github.blindpirate.gogradle.build

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.exceptions.BuildException
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithMockProcess
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils.ProcessUtilsDelegate
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
@WithMockProcess
class DefaultBuildManagerTest {
    DefaultBuildManager manager

    File resource

    ProcessUtilsDelegate delegate

    @Mock
    Project project
    @Mock
    GoBinaryManager binaryManager
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    GitDependencyManager gitDependencyManager
    @Mock
    Process process

    GolangPluginSetting setting = new GolangPluginSetting()

    String goBin

    String goroot

    @Before
    void setUp() {
        manager = new DefaultBuildManager(project, binaryManager, setting)
        when(project.getRootDir()).thenReturn(resource)
        setting.packagePath = 'root/package'

        goroot = resource.toPath().resolve('go').toAbsolutePath().toString()
        goBin = resource.toPath().resolve('go/bin/go').toAbsolutePath().toString()

        when(binaryManager.getBinaryPath()).thenReturn(resource.toPath().resolve('go/bin/go'))
        when(binaryManager.getGoroot()).thenReturn(resource.toPath().resolve('go'))

        when(delegate.run(anyList(), anyMap(), any(File))).thenReturn(process)
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream([] as byte[]))
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream([] as byte[]))

        when(project.getName()).thenReturn('project')
    }

    @Test(expected = IllegalStateException)
    void 'exception should be thrown if .vendor exists before build'() {
        IOUtils.mkdir(resource, '.vendor')
        manager.ensureDotVendorDirNotExist()
    }

    @Test
    void 'nothing should happen if .vendor not exist'() {
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
        manager.go(['test'], [:])
        // then
        assert dirDuringBuild == '.vendor'
        assert new File(resource, 'vendor').exists()
    }

    @Test(expected = BuildException)
    void 'exception should be thrown if go build return non-zero'() {
        // given
        when(process.waitFor()).thenReturn(1)
        // then
        manager.go(['test', './...'], null)
    }

    String getBuildGopath() {
        return "" + new File(resource, '.gogradle/project_gopath') + File.pathSeparator + new File(resource, '.gogradle/build_gopath')
    }

    String getTestGopath() {
        return "" + new File(resource, '.gogradle/project_gopath') +
                File.pathSeparator + new File(resource, '.gogradle/build_gopath') +
                File.pathSeparator + new File(resource, '.gogradle/test_gopath')

    }

    @Test
    void 'command args should be rendered correctly'() {
        // when
        manager.go(['build', '-o', '${GOOS}_${GOARCH}_${PROJECT_NAME}${GOEXE}'], [GOOS: 'linux', GOARCH: 'amd64', GOEXE: '', GOPATH: 'build_gopath'])
        // then
        verify(delegate).run([goBin, 'build', '-o', 'linux_amd64_project'],
                [GOOS: 'linux', GOARCH: 'amd64', GOEXE: '', GOPATH: 'build_gopath', GOROOT: goroot],
                resource)
    }

    @Test
    void 'build stdout and stderr should be redirected to logger if consumer is not specified'() {
        // given
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream('stdout\nanotherline'.getBytes(DEFAULT_CHARSET)))
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream('stderr'.getBytes(DEFAULT_CHARSET)))
        Logger mockLogger = mock(Logger)
        ReflectionUtils.setStaticFinalField(DefaultBuildManager, 'LOGGER', mockLogger)
        // when
        manager.go(['build'], [:])
        // then
        verify(mockLogger).quiet('stdout')
        verify(mockLogger).quiet('anotherline')
        verify(mockLogger).error('stderr')
        ReflectionUtils.setStaticFinalField(DefaultBuildManager, 'LOGGER', Logging.getLogger(DefaultBuildManager))
    }

    @Test
    void 'build stdout and stderr should be redirected'() {
        // given
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream('stdout\nanotherline'.getBytes(DEFAULT_CHARSET)))
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream('stderr'.getBytes(DEFAULT_CHARSET)))
        Consumer stdoutLineConsumer = mock(Consumer)
        Consumer stderrLineConsumer = mock(Consumer)
        Consumer retcodeConsumer = mock(Consumer)
        // when
        manager.go(['build'], [:], stdoutLineConsumer, stderrLineConsumer, retcodeConsumer)
        // then
        verify(stdoutLineConsumer).accept('stdout')
        verify(stdoutLineConsumer).accept('anotherline')
        verify(stderrLineConsumer).accept('stderr')
        verify(retcodeConsumer).accept(0)
    }

    @Test
    void 'copying a dependency from global cache to project cache should succeed'() {
        // given
        when(resolvedDependency.getName()).thenReturn('root/package')
        // when
        manager.installDependency(resolvedDependency, Configuration.BUILD)
        // then
        File targetDir = new File(resource, '.gogradle/build_gopath/src/root/package')
        assert targetDir.exists()
        verify(resolvedDependency).installTo(targetDir)
    }

    @Test
    void 'installing a dependency to vendor should succeed'() {
        // given
        when(resolvedDependency.getName()).thenReturn('root/package')
        // when
        manager.installDependencyToVendor(resolvedDependency)
        // then
        File targetDir = new File(resource, 'vendor/root/package')
        assert targetDir.exists()
        verify(resolvedDependency).installTo(targetDir)
    }

    @Test
    void 'target directory should be cleared before installing'() {
        // given
        when(resolvedDependency.getName()).thenReturn('root/package')
        IOUtils.write(resource, '.gogradle/build_gopath/src/root/package/oldbuildremains.go', '')
        // when
        manager.installDependency(resolvedDependency, Configuration.BUILD)
        // then
        assert !new File(resource, '.gogradle/build_gopath/src/root/package/oldbuildremains.go').exists()
    }

    @Test(expected = BuildException)
    void 'exception should be thrown if renaming vendor fails'() {
        // given
        IOUtils.mkdir(resource, 'vendor')
        IOUtils.write(resource, '.vendor', '')
        // then
        manager.go(['build'], [:])
    }

    @Test(expected = BuildException)
    void 'exception should be thrown if renaming .vendor back fails'() {
        // given
        IOUtils.mkdir(resource, 'vendor')
        when(process.getInputStream()).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                IOUtils.mkdir(resource, 'vendor')
                return new ByteArrayInputStream([] as byte[])
            }
        })
        // then
        manager.go(['build'], [:])
    }
}
