package com.github.blindpirate.gogradle.core.task;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.cache.CacheManager;
import com.github.blindpirate.gogradle.core.pack.LocalFileSystemModule;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

/**
 * This task perform preparation such as Go executable and GOPATH.
 *
 */
public class PrepareTask extends DefaultTask {

    @Inject
    private GoBinaryManager binaryManager;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private GopathManager gopathManager;

    @Inject
    private GolangPluginSetting setting;

    @TaskAction
    public void task() {
        //make sure the go binary is properly installed
        binaryManager.binaryPath();
        determineDependencies();
    }

    private void determineDependencies() {
        File rootDir = getProject().getProjectDir();
        GolangPackageModule projectModule =
                LocalFileSystemModule.fromFileSystem(
                        setting.getRootPackage(),
                        rootDir);
        projectModule.getDependencies();
    }


}
