package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.runners.model.FrameworkMethod

class WithMockGoProcessor extends GogradleRunnerProcessor<WithMockGo> {

    String mockGo = '''\
#!/usr/bin/env sh
echo 'go version go1.7.1 darwin/amd64'
'''
    String mockGoBat = '''\
echo go version go1.7.1 windows/amd64
'''
    File tmpDir

    @Override
    void beforeTest(Object instance, FrameworkMethod method, WithMockGo annotation) {
        tmpDir = GogradleRunner.tmpRandomDirectory('go')
        if (Os.getHostOs() == Os.WINDOWS) {
            IOUtils.write(tmpDir, 'go.bat', mockGoBat)
        } else {
            IOUtils.write(tmpDir, 'go', mockGo)
            IOUtils.chmodAddX(tmpDir.toPath().resolve('go'))
        }

        ReflectionUtils.setField(instance, 'goBinPath', new File(tmpDir, 'go').getAbsolutePath())
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithMockGo annotation) {
        IOUtils.deleteQuitely(tmpDir)
    }
}
