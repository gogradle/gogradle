package com.github.blindpirate.gogradle;

import org.gradle.plugins.ide.idea.model.Dependency;
import org.gradle.plugins.ide.idea.model.IdeaModule;

import java.util.Collections;
import java.util.Set;

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
        return Collections.emptySet();
    }
}
