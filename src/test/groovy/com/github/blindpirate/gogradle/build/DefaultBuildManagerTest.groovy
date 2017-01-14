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

import static com.github.blindpirate.gogradle.build.BuildManager.GOGRADLE_BUILD_DIR
import static java.nio.file.attribute.PosixFilePermissions.fromString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
@WithResource('')
class DefaultBuildManagerTest extends MockInjectorSupport {
    DefaultBuildManager manager

    File resource
    @Mock
    Project project
    @Mock
    GolangPluginSetting setting
    @Mock
    GoBinaryManager binaryManager
    @Mock
    ResolvedDependency resolvedDependency
    @Mock
    GitDependencyManager gitDependencyManager

    String mockGoBin = '''\
#!/usr/bin/env sh

echo "build started"
echo "some error occurred" >&2

if [ "-o" = "$1" -a "" != "$2" ]; then
    echo $PWD > $2
else
    echo "Usage: go -o <path>"
fi
'''

    @Before
    void setUp() {
        manager = new DefaultBuildManager(project, binaryManager, setting)
        when(project.getRootDir()).thenReturn(resource)
    }

    void prepareMockGoBin() {
        when(setting.getPackagePath()).thenReturn('root/package')
        IOUtils.write(resource, 'go', mockGoBin)
        Path mockGoBinPath = resource.toPath().resolve('go')
        Files.setPosixFilePermissions(mockGoBinPath, fromString('rwx------'))
        when(binaryManager.getBinaryPath()).thenReturn(mockGoBinPath.toString())
        when(binaryManager.getGorootEnv()).thenReturn('')
    }

    @Test
    void 'build should succeed'() {
        // given
        prepareMockGoBin()
        // when
        manager.build()
        // then
        Os hostOs = Os.getHostOs()
        Arch hostArch = Arch.getHostArch()

        Path outputFilePath = resource.toPath().resolve(GOGRADLE_BUILD_DIR).resolve("${hostOs}_${hostArch}_package")
        String outputFileContent = outputFilePath.toFile().getText()
        assert outputFileContent.trim() == resource.toPath().toString()
    }

    @Test
    void 'build stdout and stderr should be redirected to current process'() {
        // given
        Logger mockLogger = mock(Logger)
        ReflectionUtils.setStaticFinalField(DefaultBuildManager, 'LOGGER', mockLogger)
        prepareMockGoBin()
        // when
        manager.build()
        // then
        verify(mockLogger).quiet('build started')
        verify(mockLogger).error('some error occurred')
        ReflectionUtils.setStaticFinalField(DefaultBuildManager, 'LOGGER', Logging.getLogger(DefaultBuildManager))
    }

    @Test
    void 'copying a dependency from global cache to project cache should succeed'() {
        // given
        when(resolvedDependency.getName()).thenReturn('root/package')
        // when
        manager.installDependency(resolvedDependency)
        // then
        Path gopath = resource.toPath().resolve('.gogradle/build_gopath/root/package')
        assert gopath.toFile().exists()
        verify(resolvedDependency).installTo(gopath.toFile())
    }

    @Test
    void 'target directory should be cleared before installing'() {
        // given
        when(resolvedDependency.getName()).thenReturn('root/package')
        IOUtils.write(resource, '.gogradle/build_gopath/root/package/oldbuildremains.go', '')
        // when
        manager.installDependency(resolvedDependency)
        // then
        assert !resource.toPath().resolve('.gogradle/build_gopath/root/package/oldbuildremains.go').toFile().exists()
    }
}
