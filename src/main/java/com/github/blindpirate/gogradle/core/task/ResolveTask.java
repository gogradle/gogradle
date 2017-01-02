package com.github.blindpirate.gogradle.core.task;

import com.github.blindpirate.gogradle.GolangPlugin;
import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.AbstractResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeFactory;
import com.github.blindpirate.gogradle.core.pack.LocalFileSystemDependency;
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
        AbstractResolvedDependency projectModule = LocalFileSystemDependency.fromLocal(
                setting.getPackagePath(),
                rootDir);
        tree = dependencyTreeFactory.getTree(projectModule);
    }

    public DependencyTreeNode getDependencyTree() {
        return tree;
    }
}
