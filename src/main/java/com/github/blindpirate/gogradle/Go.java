/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.task.AbstractGolangTask;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.IOUtils;
import groovy.lang.Closure;
import org.apache.tools.ant.types.Commandline;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME;

/**
 * This class is designed to run custom go and other commands with build-scope environment variables,
 * such as GOPATH, GOROOT, etc. A typical usage is to declare a task:
 * <pre>
 * {@code
 * task myTask(type: Go) {
 *     dependsOn 'vendor'
 *     doLast {
 *         go 'build -o /my/output/location github.com/my/package/cmd --my-own-cmd-arguments'
 *     }
 * }
 * }
 * </pre>
 */
public class Go extends AbstractGolangTask {
    private static final Logger LOGGER = Logging.getLogger(Go.class);
    private static final Consumer<Integer> DO_NOTHING = code -> {
    };

    protected BuildManager buildManager;

    private Map<String, String> singleBuildEnvironment = new HashMap<>();

    private Map<String, String> overallEnvironment = new HashMap<>();

    private boolean continueWhenFail;

    public void setContinueWhenFail(boolean continueWhenFail) {
        this.continueWhenFail = continueWhenFail;
    }

    public Go() {
        dependsOn(PREPARE_TASK_NAME);
        buildManager = GogradleGlobal.getInstance(BuildManager.class);
    }

    public void environment(Map<String, String> map) {
        overallEnvironment.putAll(map);
    }

    public void environment(String key, String value) {
        overallEnvironment.put(key, value);
    }

    private Map<String, String> getBuildEnvironment() {
        Map<String, String> ret = new HashMap<>(overallEnvironment);
        ret.putAll(singleBuildEnvironment);
        return ret;
    }

    public Map<String, String> getSingleBuildEnvironment() {
        return singleBuildEnvironment;
    }

    public void setSingleBuildEnvironment(Map<String, String> singleBuildEnvironment) {
        this.singleBuildEnvironment = singleBuildEnvironment;
    }

    public void addDefaultActionIfNoCustomActions() {
        if (!AbstractTask.class.cast(this).isHasCustomActions()) {
            doAddDefaultAction();
        }
    }

    protected void doAddDefaultAction() {
    }

    public int go(String arg) {
        return go(arg, null, null);
    }

    public int go(List<String> args) {
        return go(args, null, null);
    }

    public int go(String arg, Closure stdoutLineConsumer) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        return go(extractArgs(arg), stdoutLineConsumer, null);
    }

    public int go(String arg, Closure stdoutLineConsumer, Closure stderrLineConsumer) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        return go(extractArgs(arg), stdoutLineConsumer, stderrLineConsumer);
    }

    public int go(List<String> args, Closure stdoutLineClosure, Closure stderrLineClosure) {
        Consumer<String> stdoutLineConsumer =
                stdoutLineClosure == null ? LOGGER::quiet : new ClosureLineConsumer(stdoutLineClosure);
        Consumer<String> stderrLineConsumer =
                stderrLineClosure == null ? LOGGER::error : new ClosureLineConsumer(stderrLineClosure);
        return buildManager.go(args,
                getBuildEnvironment(),
                stdoutLineConsumer,
                stderrLineConsumer,
                continueWhenFail ? DO_NOTHING : null);
    }

    public int run(String arg) {
        return run(arg, null, null);
    }

    public int run(List<String> args) {
        return run(args, null, null);
    }

    public int run(String arg, Closure stdoutLineClosure) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        return run(extractArgs(arg), stdoutLineClosure, null);
    }

    public int run(String arg, Closure stdoutLineClosure, Closure stderrLineClosure) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        return run(extractArgs(arg), stdoutLineClosure, stderrLineClosure);
    }

    public int run(List<String> args, Closure stdoutLineClosure, Closure stderrLineClosure) {
        Consumer<String> stdoutLineConsumer =
                stdoutLineClosure == null ? LOGGER::quiet : new ClosureLineConsumer(stdoutLineClosure);
        Consumer<String> stderrLineConsumer =
                stderrLineClosure == null ? LOGGER::error : new ClosureLineConsumer(stderrLineClosure);
        return buildManager.run(args,
                getBuildEnvironment(),
                stdoutLineConsumer,
                stderrLineConsumer,
                continueWhenFail ? DO_NOTHING : null);
    }

    public Closure appendTo(String file) {
        return new FileWritingClosure(file, true);
    }

    public Closure writeTo(String file) {
        return new FileWritingClosure(file, false);
    }

    public Closure devNull() {
        return new Closure<Void>(this) {
            public Void call(Object line) {
                return null;
            }
        };
    }

    protected List<String> extractArgs(String arg) {
        return Arrays.asList(Commandline.translateCommandline(arg));
    }

    private static class ClosureLineConsumer implements Consumer<String> {
        private Closure closure;

        private ClosureLineConsumer(Closure closure) {
            this.closure = closure;
        }

        @Override
        public void accept(String s) {
            closure.call(s);
        }
    }

    private class FileWritingClosure extends Closure<Void> {
        private File file;

        private FileWritingClosure(String file, boolean append) {
            super(Go.this);
            Path filePath = Paths.get(file);
            if (filePath.isAbsolute()) {
                this.file = filePath.toFile();
            } else {
                this.file = new File(getProject().getRootDir(), file);
            }

            if (!append) {
                IOUtils.write(this.file, "");
            }
        }

        public Void call(Object line) {
            IOUtils.append(file, "" + line + "\n");
            return null;
        }
    }


}
