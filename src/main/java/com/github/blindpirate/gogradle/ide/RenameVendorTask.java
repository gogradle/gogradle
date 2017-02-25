package com.github.blindpirate.gogradle.ide;

import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.util.DateUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

public class RenameVendorTask extends AbstractGolangTask {

    @Inject
    private Project project;

    @TaskAction
    public void renameVendor() {
        File vendor = new File(project.getRootDir(), "vendor");
        File dotVendor = new File(project.getRootDir(), ".vendor"
                + DateUtils.formatNow("yyyyMMddHHmmss"));

        if (vendor.exists() && !vendor.renameTo(dotVendor)) {
            throw new IllegalStateException("Rename from " + vendor.getAbsolutePath()
                    + " to " + dotVendor.getAbsolutePath() + " failed");
        }
    }
}
