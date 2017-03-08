package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.util.TaskUtil;
import org.gradle.plugins.ide.idea.model.Dependency;
import org.gradle.plugins.ide.idea.model.IdeaModule;

import java.util.Collections;
import java.util.Set;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.INSTALL_TEST_DEPENDENCIES_TASK_NAME;
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
        TaskUtil.runTask(getProject(), INSTALL_BUILD_DEPENDENCIES_TASK_NAME);
        TaskUtil.runTask(getProject(), INSTALL_TEST_DEPENDENCIES_TASK_NAME);
        TaskUtil.runTask(getProject(), RENAME_VENDOR_TASK_NAME);

        return Collections.emptySet();
    }

}
