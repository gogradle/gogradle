package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.GogradleRootProduceStrategy;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeFactory;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import com.github.blindpirate.gogradle.core.pack.LocalDirectoryDependency;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

import static com.github.blindpirate.gogradle.core.GolangTaskContainer.PREPARE_TASK_NAME;

public class ResolveTask extends DefaultTask {
    @Inject
    private GolangPluginSetting setting;

    @Inject
    private DependencyTreeFactory dependencyTreeFactory;

    @Inject
    private DependencyVisitor visitor;

    @Inject
    private GogradleRootProduceStrategy strategy;

    private DependencyTreeNode tree;

    public ResolveTask() {
        dependsOn(PREPARE_TASK_NAME);
    }

    @TaskAction
    public void resolve() {
        File rootDir = getProject().getRootDir();
        LocalDirectoryDependency rootProject = LocalDirectoryDependency.fromLocal(
                setting.getPackagePath(),
                rootDir);

        GolangDependencySet dependencies = strategy.produce(rootProject, rootDir, visitor);
        rootProject.setDependencies(dependencies);
        tree = dependencyTreeFactory.getTree(rootProject);
    }

    public DependencyTreeNode getDependencyTree() {
        return tree;
    }
}
