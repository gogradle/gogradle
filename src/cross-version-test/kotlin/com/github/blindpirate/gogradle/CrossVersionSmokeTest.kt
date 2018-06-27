package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.go.GoCoverTest
import org.gradle.testkit.runner.GradleRunner

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

@WithResource("go-test-cover")
@RunWith(GogradleRunner::class)
class CrossVersionSmokeTest : IntegrationTestSupport() {
    companion object {
        @JvmField
        public val VERSIONS = listOf(
//                 "3.5.1",
//                "4.0.1",
//                "4.1",
                "4.2.1", "4.3.1",
                "4.4.1", "4.5.1", "4.6", "4.7", "4.8.1")
    }

    override fun getProjectRoot(): File {
        return resource
    }

    @Test
    fun crossVersionTestShouldSucceed() {
        super.writeBuildAndSettingsDotGradle(buildDotGradleBase)

        VERSIONS.forEach {
            GradleRunner.create()
                    .withProjectDir(resource)
                    .withArguments("cover", "test", "--info")
                    .withGradleVersion(it)
                    .forwardOutput()
                    .build()
            GoCoverTest.examineCoverageHtmls(resource)
        }
    }
}
