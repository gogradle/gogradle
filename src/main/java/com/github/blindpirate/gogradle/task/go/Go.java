package com.github.blindpirate.gogradle.task.go;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.util.Assert;
import org.apache.tools.ant.types.Commandline;
import org.gradle.api.internal.AbstractTask;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Go extends AbstractGolangTask {

    @Inject
    protected BuildManager buildManager;

    private Map<String, String> env;

    private Consumer<Integer> retcodeConsumer;

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
        buildManager.go(extractArgs(arg), env, null, null, retcodeConsumer);
    }

    public void run(String arg) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        buildManager.run(extractArgs(arg), env, null, null, retcodeConsumer);
    }

    protected List<String> extractArgs(String arg) {
        return Arrays.asList(Commandline.translateCommandline(arg));
    }
}
