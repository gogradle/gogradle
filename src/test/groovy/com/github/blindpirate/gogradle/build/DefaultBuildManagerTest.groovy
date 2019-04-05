/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.build

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.exceptions.BuildException
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager
import com.github.blindpirate.gogradle.crossplatform.MockEnvironmentVariableSupport
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.vcs.git.GitDependencyManager
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
@WithMockInjector
class DefaultBuildManagerTest extends MockEnvironmentVariableSupport {
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
    Process process
    @Mock
    ProcessUtils processUtils

    GolangPluginSetting setting = new GolangPluginSetting()

    String goBin

    String goroot

    @Before
    void setUp() {
        manager = new DefaultBuildManager(project, binaryManager, setting, processUtils)
        when(project.getProjectDir()).thenReturn(resource)
        setting.packagePath = 'root/package'

        goroot = toUnixString(new File(resource, 'go'))
        goBin = toUnixString(new File(resource, 'go/bin/go'))

        when(binaryManager.getBinaryPath()).thenReturn(resource.toPath().resolve('go/bin/go'))
        when(binaryManager.getGoroot()).thenReturn(resource.toPath().resolve('go'))

        when(processUtils.run(anyList(), anyMap(), any(File))).thenReturn(process)
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream([] as byte[]))
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream([] as byte[]))

        when(project.getName()).thenReturn('project')
    }

//    @Test
//    void 'symbolic links should be created if GOPATH dose not exist'() {
//        // when
//        manager.prepareProjectGopathIfNecessary()
//        // then
//        assertSymbolicLinkLinkToTarget('.gogradle/project_gopath/src/root/package', '.')
//        assert manager.gopath == getProjectGopath()
//    }

//    @Test
//    void 'symbolic links should be created if current project dose not match GOPATH'() {
//        // when
//        environmentVariables.set('GOPATH', toUnixString(resource))
//        manager.prepareProjectGopathIfNecessary()
//        // then
//        assertSymbolicLinkLinkToTarget('.gogradle/project_gopath/src/root/package', '.')
//        assert manager.gopath == getProjectGopath()
//    }

//    @Test
//    void 'project gopath will not be created if current project matches GOPATH'() {
//        // when
//        IOUtils.mkdir(resource, 'src/root/package')
//
//        String systemGopath = 'fakegopath' + File.pathSeparator + toUnixString(resource)
//        environmentVariables.set('GOPATH', systemGopath)
//        when(project.getProjectDir()).thenReturn(new File(resource, 'src/root/package'))
//
//        manager.prepareProjectGopathIfNecessary()
//        // then
//        assert !new File(resource, 'src/root/package/.gogradle/project_gopath').exists()
//        assert manager.gopath == systemGopath
//    }
//
//    void assertSymbolicLinkLinkToTarget(String link, String target) {
//        Path linkPath = resource.toPath().resolve(link)
//        Path targetPath = resource.toPath().resolve(target)
//        Path relativePathOfLink = Files.readSymbolicLink(linkPath)
//        assert linkPath.getParent().resolve(relativePathOfLink).normalize() == targetPath.normalize()
//    }

    @Test(expected = BuildException)
    void 'exception should be thrown if go build return non-zero'() {
        // given
        when(process.waitFor()).thenReturn(1)
        // then
        manager.go(['test', './...'], [:])
    }

    String getProjectGopath() {
        return toUnixString(resource.getAbsolutePath()) + ":" + setting.getVendorTargetDir()
    }

    @Test
    void 'customized command should succeed'() {
        // given
        setting.setVendorTargetDir(resource.getAbsolutePath())
        manager.prepareProjectGopathIfNecessary()
        setting.buildTags = ['a', 'b', 'c']
        // when
        int retcode = manager.run(['golint'], [:], null, null)
        // then
        verify(processUtils).run(['golint'],
                [GOPATH: getProjectGopath(),
                 GOROOT: getGoroot(),
                 GOOS  : Os.getHostOs().toString(),
                 GOARCH: Arch.getHostArch().toString(),
                 GOEXE : Os.getHostOs().exeExtension()],
                resource)
        assert retcode == 0
    }

    @Test
    void 'build tags should succeed'() {
        // given
        setting.buildTags = ['a', 'b', 'c']
        // when
        manager.go(['build', '-o', '${GOOS}_${GOARCH}_${PROJECT_NAME}${GOEXE}'], [GOOS: 'linux', GOARCH: 'amd64', GOEXE: '', GOPATH: 'project_gopath'])
        // then
        verify(processUtils).run([goBin, 'build', '-tags', "'a b c'", '-o', 'linux_amd64_project'],
                [GOOS: 'linux', GOARCH: 'amd64', GOEXE: '', GOPATH: 'project_gopath', GOROOT: goroot],
                resource)
    }

    @Test
    void 'command args should be rendered correctly'() {
        // when
        manager.go(['build', '-o', '${GOOS}_${GOARCH}_${PROJECT_NAME}${GOEXE}'], [GOOS: 'linux', GOARCH: 'amd64', GOEXE: '', GOPATH: 'project_gopath'])
        // then
        verify(processUtils).run([goBin, 'build', '-o', 'linux_amd64_project'],
                [GOOS: 'linux', GOARCH: 'amd64', GOEXE: '', GOPATH: 'project_gopath', GOROOT: goroot],
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
        // when
        int retcode = manager.go(['build'], [:], stdoutLineConsumer, stderrLineConsumer)
        // then
        verify(stdoutLineConsumer).accept('stdout')
        verify(stdoutLineConsumer).accept('anotherline')
        verify(stderrLineConsumer).accept('stderr')
        assert retcode == 0
    }

    @Test
    void 'inserting build tags should succeed'() {
        setting.buildTags = ['a', 'b', 'c']

        assert manager.insertBuildTags([]) == []
        assert manager.insertBuildTags(['build']) == ['build', '-tags', "'a b c'"]
        assert manager.insertBuildTags(['build', 'package']) == ['build', '-tags', "'a b c'", 'package']
        assert manager.insertBuildTags(['tool', 'vet', 'package']) == ['tool', 'vet', '-tags', "'a b c'", 'package']
        assert manager.insertBuildTags(['tool', 'cover', 'package']) == ['tool', 'cover', 'package']
    }

//    @Test(expected = BuildException)
//    void 'exception should be thrown if creating symbolic link fails'() {
//        manager.createSymbolicLink(resource.toPath(), resource.toPath())
//    }

//    @Test
//    void 'nothing should happen if symbolic link exists'() {
//        IOUtils.write(resource, '.gogradle/project_gopath/root/package', '')
//        manager.prepareProjectGopathIfNecessary()
//    }
}
