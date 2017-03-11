package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.util.Assert;
import org.apache.tools.ant.types.Commandline;
import org.gradle.api.internal.AbstractTask;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.CollectionUtils.asStringList;

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

    public Object methodMissing(String name, Object args) {
        Object[] argsArray = (Object[]) args;

        if ("go".equals(name)) {
            Assert.isTrue(argsArray.length == 1, "Args must be put into a single string: go 'build -v -a");
            buildManager.go(extractArgs(argsArray[0].toString()), currentEnv);
        } else if ("run".equals(name)) {
            Assert.isTrue(argsArray.length == 1, "Args must be put into a single string: run 'go build -v -a");
            buildManager.run(extractArgs(argsArray[0].toString()), currentEnv);
        } else {
            Assert.isTrue(argsArray.length == 1, "Args must be put into a single string: golint 'package/path/name'");
            buildManager.run(asStringList(name, extractArgs(argsArray[0].toString())), currentEnv);
        }
        return null;
    }

    private List<String> extractArgs(String arg) {
        return Arrays.asList(Commandline.translateCommandline(arg));
    }
}
