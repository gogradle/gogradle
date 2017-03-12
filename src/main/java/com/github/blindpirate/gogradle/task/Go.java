package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.util.Assert;
import org.apache.tools.ant.types.Commandline;
import org.gradle.api.internal.AbstractTask;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Go extends AbstractGolangTask {

    @Inject
    protected BuildManager buildManager;

    private Map<String, String> currentEnv;

    public void setCurrentEnv(Map<String, String> currentEnv) {
        this.currentEnv = currentEnv;
    }

    public Map<String, String> getCurrentEnv() {
        return currentEnv;
    }

    public void addDefaultActionIfNoCustomActions() {
        if (!AbstractTask.class.cast(this).isHasCustomActions()) {
            doAddDefaultAction();
        }
    }

    protected void doAddDefaultAction() {
    }

    public void go(String arg) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        buildManager.go(extractArgs(arg), currentEnv);
    }

    public void run(String arg) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        buildManager.run(extractArgs(arg), currentEnv);
    }

    private List<String> extractArgs(String arg) {
        return Arrays.asList(Commandline.translateCommandline(arg));
    }
}
