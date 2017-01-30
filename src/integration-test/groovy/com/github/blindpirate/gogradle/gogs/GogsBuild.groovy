package com.github.blindpirate.gogradle.gogs

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.crossplatform.Arch
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.vcs.git.GitAccessor
import org.eclipse.jgit.lib.Repository
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

import java.nio.file.Files
import java.nio.file.Path

import static com.github.blindpirate.gogradle.util.ProcessUtils.getResult
import static com.github.blindpirate.gogradle.util.ProcessUtils.run
import static java.nio.file.attribute.PosixFilePermissions.fromString

@RunWith(GogradleRunner)
@WithResource('')
class GogsBuild extends IntegrationTestSupport {
    File resource = new File('/Users/zhb/Develop/top1000/gogits_gogs')

    GitAccessor gitAccessor = new GitAccessor()

    String buildDotGradle = """
${buildDotGradleBase}
golang {
    packagePath="github.com/gogits/gogs"
}
"""

    @Test
    @AccessWeb
    @Ignore
    void 'gogs should be built successfully'() {
//        gitAccessor.cloneWithUrl('https://github.com/gogits/gogs.git', resource)
        Repository repository = gitAccessor.getRepository(resource)
        // v0.9.113
        gitAccessor.checkout(repository, '114c179e5a50e3313f7a5894100693805e64e440')

        IOUtils.write(resource, 'build.gradle', buildDotGradle)

        newBuild {
            it.forTasks('test')
//            it.forTasks('dependencies')
        }

        println(stdout)


        Path gogsBinPath = resource.toPath().resolve(".gogradle/${Os.getHostOs()}_${Arch.getHostArch()}_gogs")
        Files.setPosixFilePermissions(gogsBinPath, fromString('rwx------'))

        Process process = run([gogsBinPath.toFile().absolutePath], [:])
        println(getResult(process).stdout)
    }

    @Override
    List<String> buildArguments() {
        return super.buildArguments()
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
