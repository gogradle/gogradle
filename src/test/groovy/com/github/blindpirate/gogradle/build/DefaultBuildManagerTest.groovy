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
import com.github.blindpirate.gogradle.vcs.git.GitDependencyResolver
import org.gradle.api.Project
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.nio.file.Files
import java.nio.file.Path

import static com.github.blindpirate.gogradle.build.BuildManager.GOGRADLE_BUILD_DIR
import static java.nio.file.attribute.PosixFilePermissions.fromString
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
    GitDependencyResolver resolver

    String mockGoBin = '''\
#!/usr/bin/env sh

if [ "-o" == "$1" -a "" != "$2" ]; then
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

    @Test
    void 'build should succeed'() {
        // given
        IOUtils.write(resource, 'go', mockGoBin)
        Path mockGoBinPath = resource.toPath().resolve('go')
        Files.setPosixFilePermissions(mockGoBinPath, fromString('rwx------'))
        when(binaryManager.binaryPath()).thenReturn(mockGoBinPath.toString())
        when(setting.getPackagePath()).thenReturn('root/package')
        // when
        manager.build()
        // then
        Os hostOs = Os.getHostOs()
        Arch hostArch = Arch.getHostArch()

        Path outputFilePath = resource.toPath().resolve(GOGRADLE_BUILD_DIR).resolve("${hostOs}_${hostArch}_package")
        String outputFileContent = IOUtils.toString(outputFilePath.toFile())
        assert outputFileContent.trim() == resource.toPath().toString()
    }

    @Test
    void 'copying a dependency from global cache to project cache should succeed'() {
        // given
        when(resolvedDependency.getName()).thenReturn('root/package')
        when(resolvedDependency.getResolverClass()).thenReturn(GitDependencyResolver)
        when(injector.getInstance(GitDependencyResolver)).thenReturn(resolver)
        // when
        manager.installDependency(resolvedDependency)
        // then
        Path gopath = resource.toPath().resolve('.gogradle/build_gopath/root/package')
        assert gopath.toFile().exists()
        verify(resolver).reset(resolvedDependency, gopath.toFile())
    }
}
