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

package com.github.blindpirate.gogradle.crossplatform

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.GolangPluginSetting
import com.github.blindpirate.gogradle.core.cache.GlobalCacheManager
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.HttpUtils
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ProcessUtils
import com.github.blindpirate.gogradle.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Files
import java.nio.file.Path

import static org.mockito.Mockito.*

@RunWith(GogradleRunner)
@WithResource('')
class DefaultGoBinaryManagerTest extends MockEnvironmentVariableSupport {
    @Mock
    GolangPluginSetting setting
    @Mock
    HttpUtils httpUtils
    @Mock
    ProcessUtils.ProcessResult processResult
    @Mock
    GlobalCacheManager cacheManager
    @Mock
    ProcessUtils processUtils

    File resource

    InputStream mockGoTarGz = getClass().classLoader.getResourceAsStream('mock-go-1.7.4' + Os.getHostOs().archiveExtension())

    DefaultGoBinaryManager manager

    @Before
    void setUp() {
        Process process = mock(Process)
        when(setting.getGoExecutable()).thenReturn("go")
        when(processUtils.run(['go', 'version'], null, null)).thenReturn(process)
        when(processUtils.getResult(process)).thenReturn(processResult)

        when(cacheManager.getGlobalGoBinCacheDir(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                return resource.toPath().resolve(invocation.getArgument(0)).toFile()
            }
        })

        when(httpUtils.download(anyString(), any(Path))).thenAnswer(new Answer<Object>() {
            @Override
            Object answer(InvocationOnMock invocation) throws Throwable {
                Path filePath = invocation.getArgument(1)
                Files.copy(mockGoTarGz, filePath)
                return null
            }
        })
        manager = new DefaultGoBinaryManager(setting, cacheManager, httpUtils, processUtils)
    }

    private turnOnMockGo() {
        IOUtils.write(resource, "go/bin/go${Os.getHostOs().exeExtension()}", '')
        environmentVariables.set('PATH', new File(resource, 'go/bin').absolutePath)
        Process process = mock(Process)
        when(processUtils.run([new File(resource, "go/bin/go${Os.getHostOs().exeExtension()}").absolutePath, 'version'] as String[])).thenReturn(process)
        when(processUtils.getResult(process)).thenReturn(processResult)
        when(processResult.getStdout()).thenReturn('go version go1.7.1 darwin/amd64')
    }

    @Test(expected = IllegalStateException)
    void 'user-specified go binary should cause an exception if it cannot be executed'() {
        // given
        when(processUtils.run(['/unexistent/go', 'version'], null, null)).thenThrow(IOException)
        when(setting.getGoExecutable()).thenReturn('/unexistent/go')
        // then
        'the newest stable version will be used if local binary not exist and no version specified'()
    }

    @Test
    void 'goroot should be used if it is specified'() {
        // given
        turnOnMockGo()
        when(setting.getGoRoot()).thenReturn(resource.absolutePath)
        // then
        assert manager.getGoroot() == resource.toPath()
    }

    @Test
    void 'user-specific go binary should be used if it match specific version'() {
        // given
        turnOnMockGo()
        File mockGo = new File(resource, "go/bin/go${Os.getHostOs().exeExtension()}")
        when(setting.getGoExecutable()).thenReturn(StringUtils.toUnixString(mockGo))
        when(setting.getGoVersion()).thenReturn('1.7.1')
        // then
        assert manager.getGoVersion() == '1.7.1'
        assert manager.getBinaryPath() == mockGo.toPath()
    }

    @Test(expected = IllegalStateException)
    void 'exception should be throw if user-specific go version not match required version'() {
        // given
        turnOnMockGo()
        File mockGo = new File(resource, "go/bin/go${Os.getHostOs().exeExtension()}")
        when(setting.getGoExecutable()).thenReturn(StringUtils.toUnixString(mockGo))
        when(setting.getGoVersion()).thenReturn('1.8')
        // then
        manager.getGoVersion()
    }

    @Test
    void 'local go binary should be ignored if IGNORE_LOCAL is specified'() {
        // given
        turnOnMockGo()
        when(setting.getGoExecutable()).thenReturn('IGNORE_LOCAL')
        // then
        'the newest stable version will be used if local binary not exist and no version specified'()
    }

    @Test
    void 'local go binary should be ignored if it cannot be recognized'() {
        // given
        turnOnMockGo()
        when(processResult.getStdout()).thenReturn('this is not a golang executable')
        // then
        'the newest stable version will be used if local binary not exist and no version specified'()
    }

    @Test
    void 'local go binary should be returned if it exists and no version specified'() {
        // given
        turnOnMockGo()
        // then
        assert manager.getBinaryPath() == resource.toPath().resolve("go/bin/go${Os.getHostOs().exeExtension()}")
        assert manager.getGoVersion() == '1.7.1'
        assert manager.getGoroot() == resource.toPath().resolve('go')
    }

    @Test
    void 'local go binary should be returned if specified version is exactly local version'() {
        // given
        turnOnMockGo()
        when(setting.getGoVersion()).thenReturn('1.7.1')
        // then
        assert manager.getBinaryPath() == resource.toPath().resolve("go/bin/go${Os.getHostOs().exeExtension()}")
        assert manager.getGoVersion() == '1.7.1'
        assert manager.getGoroot() == resource.toPath().resolve('go')
    }

    @Test
    void 'the newest stable version will be used if local binary not exist and no version specified'() {
        // given
        when(httpUtils.get(anyString())).thenReturn('1.7.4')
        // then
        assert manager.getBinaryPath() == resource.toPath().resolve("1.7.4/go/bin/go${Os.getHostOs().exeExtension()}")
        assert manager.getGoVersion() == '1.7.4'
        assert manager.getGoroot() == resource.toPath().resolve('1.7.4/go')
        verify(httpUtils).download(anyString(), any(Path))
    }

    @Test
    void 'go binary in global cache should be returned directly if it has already existed'() {
        // given
        when(httpUtils.get(anyString())).thenReturn('1.7.4')
        IOUtils.write(resource, "1.7.4/go/bin/go${Os.getHostOs().exeExtension()}", 'mock go binary')
        // then
        assert manager.getBinaryPath() == resource.toPath().resolve("1.7.4/go/bin/go${Os.getHostOs().exeExtension()}")
        assert manager.getGoVersion() == '1.7.4'
        assert manager.getGoroot() == resource.toPath().resolve('1.7.4/go')
        verify(httpUtils, times(0)).download(anyString(), any(Path))
    }

    @Test
    void 'go with specific version should be downloaded if it does not match go version on host'() {
        // given
        turnOnMockGo()
        'go binary with specified version should be downloaded'()
    }

    @Test
    void 'go binary with specified version should be downloaded'() {
        // given
        when(setting.getGoVersion()).thenReturn("1.7.4")
        // then
        assert manager.getBinaryPath() == resource.toPath().resolve("1.7.4/go/bin/go${Os.getHostOs().exeExtension()}")
        assert manager.getGoVersion() == '1.7.4'
        assert manager.getGoroot() == resource.toPath().resolve('1.7.4/go')
        verify(httpUtils).download(anyString(), any(Path))
    }

    @Test
    void 'go binary with specified version should be downloaded when a modified go binary base url has been set'() {
        // given
        when(setting.getGoVersion()).thenReturn('1.7.4')
        when(setting.getGoBinaryDownloadBaseUri()).thenReturn(URI.create('http://example.com/'))
        // then
        assert manager.getBinaryPath() == resource.toPath().resolve("1.7.4/go/bin/go${Os.getHostOs().exeExtension()}")
        assert manager.getGoVersion() == '1.7.4'
        assert manager.getGoroot() == resource.toPath().resolve('1.7.4/go')
        verify(httpUtils).download(eq("http://example.com/go1.7.4.${Os.getHostOs().toString()}-${Arch.getHostArch().toString()}${Os.getHostOs().archiveExtension()}".toString()), any(Path))
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown when specified version is invalid'() {
        // given
        when(setting.getGoVersion()).thenReturn('999.999.999')
        when(httpUtils.download(anyString(), any(Path))).thenThrow(new IOException())
        // then
        manager.getBinaryPath()
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown when download fails'() {
        // given
        when(httpUtils.get(anyString())).thenReturn('1.7.4')
        when(httpUtils.download(anyString(), any(Path))).thenThrow(new IOException())
        // then
        manager.getBinaryPath()
    }

    @Test(expected = UncheckedIOException)
    void 'exception should be thrown when getting version fails'() {
        // given
        when(httpUtils.get(anyString())).thenThrow(new IOException())
        // then
        manager.getBinaryPath()
    }

}
