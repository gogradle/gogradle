package com.github.blindpirate.gogradle.core.task;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.FileSystemPackageModule;
import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.pack.LocalFileSystemPackageModule;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.core.cache.CacheDirectoryManager;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

/**
 * This task perform preparation such as Go executable and GOPATH.
 */
class PrepareTask extends DefaultTask {

    @Inject
    private GoBinaryManager binaryManager;

    @Inject
    private CacheDirectoryManager cacheDirectoryManager;

    @Inject
    private GopathManager gopathManager;

    @Inject
    GolangPluginSetting setting;

    @TaskAction
    public void task() {
        //make sure the go binary is properly installed
        binaryManager.binaryPath();
        determineDependencies();
    }

    private void determineDependencies() {
        File rootDir = getProject().getProjectDir();
        GolangPackageModule projectModule =
                LocalFileSystemPackageModule.fromFileSystem(
                        setting.getRootPackage(),
                        rootDir);
    }

}
