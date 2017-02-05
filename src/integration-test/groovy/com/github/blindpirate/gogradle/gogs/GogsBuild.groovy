package com.github.blindpirate.gogradle.gogs

import com.github.blindpirate.gogradle.GitRepositoryHandler
import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.OnlyWhen
import com.github.blindpirate.gogradle.support.WithMockInjector
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.vcs.git.GitAccessor
import org.eclipse.jgit.lib.Repository
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Path

import static com.github.blindpirate.gogradle.util.ProcessUtils.getResult
import static com.github.blindpirate.gogradle.util.ProcessUtils.run

@RunWith(GogradleRunner)
@WithMockInjector
@OnlyWhen("System.getenv('GOGS_DIR')!=null")
class GogsBuild extends IntegrationTestSupport {
    File resource = new File(System.getenv('GOGS_DIR'))

    GitAccessor gitAccessor = new GitAccessor(new GitRepositoryHandler())

    String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath="github.com/gogits/gogs"
}
"""

    @Test
    @AccessWeb
    void 'gogs should be built successfully'() {
        Repository repository = gitAccessor.getRepository(resource)
        // v0.9.113
        gitAccessor.checkout(repository, '114c179e5a50e3313f7a5894100693805e64e440')

        IOUtils.write(resource, 'build.gradle', buildDotGradle)

        try {
            newBuild {
                it.forTasks('build')
            }
        } finally {
            println(stdout)
            println(stderr)
        }

        Path gogsBinPath = resource.toPath().resolve(".gogradle/${Os.getHostOs()}_${Arch.getHostArch()}_gogs")
        if (Os.getHostOs() == Os.WINDOWS) {
            gogsBinPath.renameTo(gogsBinPath.toString() + '.exe')
        }
        IOUtils.chmodAddX(gogsBinPath)

        Process process = run([gogsBinPath.toFile().absolutePath], [:])
        assert getResult(process).stdout.contains('Gogs')
    }

    @Override
    List<String> buildArguments() {
        goBinPath = ''
        return super.buildArguments()
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
