package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.common.LineCollector;
import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.util.Assert;
import groovy.lang.Closure;
import org.apache.tools.ant.types.Commandline;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME;

public class Go extends AbstractGolangTask {
    private static final Logger LOGGER = Logging.getLogger(Go.class);
    private static final Consumer<Integer> DO_NOTHING = code -> {
    };

    protected BuildManager buildManager;

    private Map<String, String> env;

    private boolean continueWhenFail;

    public void setContinueWhenFail(boolean continueWhenFail) {
        this.continueWhenFail = continueWhenFail;
    }

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

    public void addDefaultActionIfNoCustomActions() {
        if (!AbstractTask.class.cast(this).isHasCustomActions()) {
            doAddDefaultAction();
        }
    }

    protected void doAddDefaultAction() {
    }

    public void go(String arg) {
        go(arg, null);
    }

    public void go(List<String> args) {
        go(args, null);
    }

    public void go(String arg, Closure stdoutStderrConsumer) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        go(extractArgs(arg), stdoutStderrConsumer);
    }

    public void go(List<String> args, Closure stdoutStderrConsumer) {
        Consumer<String> stdoutLineConsumer = stdoutStderrConsumer == null ? LOGGER::quiet : new LineCollector();
        Consumer<String> stderrLineConsumer = stdoutStderrConsumer == null ? LOGGER::error : new LineCollector();
        buildManager.go(args, env, stdoutLineConsumer, stderrLineConsumer, continueWhenFail ? DO_NOTHING : null);

        processStdoutStderrIfNecessary(stdoutStderrConsumer, stdoutLineConsumer, stderrLineConsumer);
    }

    public void run(String arg) {
        run(arg, null);
    }

    public void run(List<String> args) {
        run(args, null);
    }

    public void run(String arg, Closure stdoutStderrConsumer) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        run(extractArgs(arg), stdoutStderrConsumer);
    }

    public void run(List<String> args, Closure stdoutStderrConsumer) {
        Consumer<String> stdoutLineConsumer = stdoutStderrConsumer == null ? LOGGER::quiet : new LineCollector();
        Consumer<String> stderrLineConsumer = stdoutStderrConsumer == null ? LOGGER::error : new LineCollector();
        buildManager.run(args, env, stdoutLineConsumer, stderrLineConsumer, continueWhenFail ? DO_NOTHING : null);

        processStdoutStderrIfNecessary(stdoutStderrConsumer, stdoutLineConsumer, stderrLineConsumer);

    }

    private void processStdoutStderrIfNecessary(Closure stdoutStderrConsumer,
                                                Consumer<String> stdoutLineConsumer,
                                                Consumer<String> stderrLineConsumer) {
        if (stdoutStderrConsumer != null) {
            String stdout = LineCollector.class.cast(stdoutLineConsumer).getOutput();
            String stderr = LineCollector.class.cast(stderrLineConsumer).getOutput();
            if (stdoutStderrConsumer.getMaximumNumberOfParameters() == 1) {
                stdoutStderrConsumer.call(stdout);
            } else if (stdoutStderrConsumer.getMaximumNumberOfParameters() == 2) {
                stdoutStderrConsumer.call(stdout, stderr);
            }
        }
    }

    protected List<String> extractArgs(String arg) {
        return Arrays.asList(Commandline.translateCommandline(arg));
    }

}
