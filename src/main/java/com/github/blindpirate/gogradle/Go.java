package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.util.Assert;
import org.apache.tools.ant.types.Commandline;
import org.gradle.api.internal.AbstractTask;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME;

public class Go extends AbstractGolangTask {

    protected BuildManager buildManager;

    private Map<String, String> env;

    private Consumer<Integer> retcodeConsumer;

    public Go() {
        dependsOn(PREPARE_TASK_NAME);
        buildManager = GogradleGlobal.getInstance(BuildManager.class);
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    public Consumer<Integer> getRetcodeConsumer() {
        return retcodeConsumer;
    }

    public void setRetcodeConsumer(Consumer<Integer> retcodeConsumer) {
        this.retcodeConsumer = retcodeConsumer;
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
        go(extractArgs(arg));
    }

    public void go(List<String> args) {
        buildManager.go(args, env, null, null, retcodeConsumer);
    }

    public void run(String arg) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        run(extractArgs(arg));
    }

    public void run(List<String> args) {
        buildManager.run(args, env, null, null, retcodeConsumer);
    }

    protected List<String> extractArgs(String arg) {
        return Arrays.asList(Commandline.translateCommandline(arg));
    }
}
