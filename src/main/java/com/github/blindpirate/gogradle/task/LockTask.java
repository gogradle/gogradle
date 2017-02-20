package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.lock.LockedDependencyManager;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_BUILD_DEPENDENCIES_TASK_NAME;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.RESOLVE_TEST_DEPENDENCIES_TASK_NAME;

public class LockTask extends AbstractGolangTask {

    @Inject
    private LockedDependencyManager lockedDependencyManager;

    public LockTask() {
        dependsOn(RESOLVE_BUILD_DEPENDENCIES_TASK_NAME,
                RESOLVE_TEST_DEPENDENCIES_TASK_NAME);
    }

    @TaskAction
    public void lock() {
        DependencyTreeNode buildDependencyTree = getTask(ResolveBuildDependenciesTask.class).getDependencyTree();
        DependencyTreeNode testDependencyTree = getTask(ResolveTestDependenciesTask.class).getDependencyTree();

        lockedDependencyManager.lock(toResolveDependencySet(buildDependencyTree),
                toResolveDependencySet(testDependencyTree));
    }

    private Set<ResolvedDependency> toResolveDependencySet(DependencyTreeNode root) {
        return root.flatten().stream()
                .map(dependency -> (ResolvedDependency) dependency)
                .collect(Collectors.toSet());
    }
}
