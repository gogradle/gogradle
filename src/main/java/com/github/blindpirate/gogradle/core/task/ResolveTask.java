package com.github.blindpirate.gogradle.core.task;

import com.github.blindpirate.gogradle.GolangPlugin;
import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyTreeNode;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyTreeFactory;
import com.github.blindpirate.gogradle.core.pack.LocalFileSystemModule;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

public class ResolveTask extends DefaultTask {
    @Inject
    private GolangPluginSetting setting;

    @Inject
    private DependencyTreeFactory dependencyTreeFactory;

    private DependencyTreeNode tree;

    public ResolveTask() {
        dependsOn(GolangPlugin.PREPARE_TASK_NAME);
    }

    @TaskAction
    public void resolve() {
        File rootDir = getProject().getRootDir();
        GolangPackageModule projectModule = LocalFileSystemModule.fromFileSystem(
                setting.getPackageName(),
                rootDir);
        tree = dependencyTreeFactory.getTree(projectModule);
    }

    public DependencyTreeNode getDependencyTree() {
        return tree;
    }
}
