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

package com.github.blindpirate.gogradle.support

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.crossplatform.Os
import com.github.blindpirate.gogradle.util.IOUtils
import com.github.blindpirate.gogradle.util.ReflectionUtils
import com.github.blindpirate.gogradle.util.StringUtils
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
            ReflectionUtils.setField(instance, 'goBinPath', StringUtils.toUnixString(new File(tmpDir, 'go.bat')))
        } else {
            IOUtils.write(tmpDir, 'go', mockGo)
            IOUtils.chmodAddX(tmpDir.toPath().resolve('go'))
            ReflectionUtils.setField(instance, 'goBinPath', StringUtils.toUnixString(new File(tmpDir, 'go')))
        }
    }

    @Override
    void afterTest(Object instance, FrameworkMethod method, WithMockGo annotation) {
        IOUtils.deleteQuitely(tmpDir)
    }
}
