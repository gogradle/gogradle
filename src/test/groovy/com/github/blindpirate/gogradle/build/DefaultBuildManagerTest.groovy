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

if [ "build" = "$1" -a "-o" = "$2" ] && [ ""!="$3" ]; then
    echo $PWD > $3
    echo $GOPATH >> $3
else
    echo "Usage: go -o <path>"
fi
'''

    @Before
    void setUp() {
        manager = new DefaultBuildManager(project, binaryManager, setting)
        when(project.getRootDir()).thenReturn(resource)
        when(setting.getPackagePath()).thenReturn('root/package')
    }

    void prepareMockGoBin() {
        IOUtils.forceMkdir(resource.toPath().resolve('.gogradle').toFile())
        IOUtils.write(resource, 'go', mockGoBin)
        Path mockGoBinPath = resource.toPath().resolve('go')
        Files.setPosixFilePermissions(mockGoBinPath, fromString('rwx------'))
        when(binaryManager.getBinaryPath()).thenReturn(mockGoBinPath.toString())
        when(binaryManager.getGorootEnv()).thenReturn('')
    }

    @Test
    void 'symbolic links should be created properly in preparation'() {
        // when
        manager.prepareForBuild()
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
    void 'build should succeed'() {
        // given
        prepareMockGoBin()
        // when
        manager.build()
        // then
        Os hostOs = Os.getHostOs()
        Arch hostArch = Arch.getHostArch()

        Path outputFilePath = resource.toPath().resolve(".gogradle/${hostOs}_${hostArch}_package")
        List<String> lines = IOUtils.getLines(outputFilePath.toFile())
        assert lines[0].trim() == resource.toPath().toString()
        assert lines[1].trim() == "" + resource.toPath().resolve('.gogradle/project_gopath') + File.pathSeparator + resource.toPath().resolve('.gogradle/build_gopath')
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
