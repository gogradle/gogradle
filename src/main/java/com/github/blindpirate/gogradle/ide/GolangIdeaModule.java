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

package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.util.TaskUtil;
import org.gradle.plugins.ide.idea.model.Dependency;
import org.gradle.plugins.ide.idea.model.IdeaModule;

import java.util.Collections;
import java.util.Set;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.IDEA_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RENAME_VENDOR_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME;

public class GolangIdeaModule extends IdeaModule {
    public GolangIdeaModule(IdeaModule ideaModule) {
        super(ideaModule.getProject(), ideaModule.getIml());
        setName(ideaModule.getName());
        setSourceDirs(ideaModule.getSourceDirs());
        setGeneratedSourceDirs(ideaModule.getGeneratedSourceDirs());
        setScopes(ideaModule.getScopes());
        setDownloadJavadoc(ideaModule.isDownloadJavadoc());
        setDownloadSources(ideaModule.isDownloadSources());
        setContentRoot(ideaModule.getContentRoot());
        setTestSourceDirs(ideaModule.getTestSourceDirs());
        setExcludeDirs(ideaModule.getExcludeDirs());
        setInheritOutputDirs(ideaModule.getInheritOutputDirs());
        setOutputDir(ideaModule.getOutputDir());
        setTestOutputDir(ideaModule.getTestOutputDir());
        setPathVariables(ideaModule.getPathVariables());
        setJdkName(ideaModule.getJdkName());
        setLanguageLevel(ideaModule.getLanguageLevel());
        setTargetBytecodeVersion(ideaModule.getTargetBytecodeVersion());
        setScalaPlatform(ideaModule.getScalaPlatform());
        setPathFactory(ideaModule.getPathFactory());
        setOffline(ideaModule.isOffline());
        setSingleEntryLibraries(ideaModule.getSingleEntryLibraries());
    }

    @Override
    public Set<Dependency> resolveDependencies() {
        TaskUtil.runTask(getProject(), PREPARE_TASK_NAME);
        TaskUtil.runTask(getProject(), RESOLVE_BUILD_DEPENDENCIES_TASK_NAME);
        TaskUtil.runTask(getProject(), RESOLVE_TEST_DEPENDENCIES_TASK_NAME);
        TaskUtil.runTask(getProject(), RENAME_VENDOR_TASK_NAME);
        TaskUtil.runTask(getProject(), IDEA_TASK_NAME);

        return Collections.emptySet();
    }

}
