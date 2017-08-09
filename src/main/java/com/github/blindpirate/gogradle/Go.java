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
import com.github.blindpirate.gogradle.util.CollectionUtils;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.google.inject.Injector;
import groovy.lang.Closure;
import org.apache.tools.ant.types.Commandline;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.github.blindpirate.gogradle.GolangPlugin.GOGRADLE_INJECTOR;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME;

/**
 * This class is designed to run custom go and other commands with build-scope environment variables,
 * such as GOPATH, GOROOT, etc. A typical usage is to declare a task:
 * <pre>
 * {@code
 * task myTask(type: Go) {
 *     dependsOn 'vendor'
 *     go 'build -o /my/output/location github.com/my/package/cmd --my-own-cmd-arguments'
 * }
 * }
 * </pre>
 */
public class Go extends AbstractGolangTask {
    private static final Logger LOGGER = Logging.getLogger(Go.class);

    private static final String GO = "GO";

    protected BuildManager buildManager;

    protected List<String> commandLineArgs;

    protected Consumer<String> stdoutLineConsumer;

    protected Consumer<String> stderrLineConsumer;

    protected Map<String, String> environment = new HashMap<>();

    protected boolean continueWhenFail;

    private int exitValue = -1;

    public Go() {
        setDescription("Custom go task.");
        dependsOn(PREPARE_TASK_NAME);
        Injector injector = (Injector) getProject().getExtensions().getByName(GOGRADLE_INJECTOR);
        this.buildManager = injector.getInstance(BuildManager.class);
    }

    @TaskAction
    public void executeTask() {
        if (CollectionUtils.isEmpty(commandLineArgs)) {
            return;
        }
        if (GO == commandLineArgs.get(0)) {
            exitValue = buildManager.go(commandLineArgs.subList(1, commandLineArgs.size()),
                    environment,
                    stdoutLineConsumer,
                    stderrLineConsumer,
                    continueWhenFail);
        } else {
            exitValue = buildManager.run(commandLineArgs,
                    environment,
                    stdoutLineConsumer,
                    stderrLineConsumer,
                    continueWhenFail);
        }
    }

    public void environment(Map<String, String> map) {
        environment.putAll(map);
    }

    public void environment(String key, String value) {
        environment.put(key, value);
    }

    public int getExitValue() {
        return exitValue;
    }

    public void setContinueWhenFail(boolean continueWhenFail) {
        this.continueWhenFail = continueWhenFail;
    }

    public void go(String arg) {
        go(arg, null, null);
    }

    public void go(List<String> args) {
        go(args, (Closure) null, null);
    }

    public void go(String arg, Closure stdoutLineConsumer) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        go(extractArgs(arg), stdoutLineConsumer, null);
    }

    public void go(String arg, Closure stdoutLineConsumer, Closure stderrLineConsumer) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        go(extractArgs(arg), stdoutLineConsumer, stderrLineConsumer);
    }

    public void go(List<String> args, Closure stdoutLineClosure, Closure stderrLineClosure) {
        run(CollectionUtils.asStringList(GO, args), stdoutLineClosure, stderrLineClosure);
    }

    public void go(List<String> args, Consumer<String> stdoutLineConsumer, Consumer<String> stderrLineConsumer) {
        run(CollectionUtils.asStringList(GO, args), stdoutLineConsumer, stderrLineConsumer);
    }

    public void run(String arg) {
        run(arg, null, null);
    }

    public void run(List<String> args) {
        run(args, (Closure) null, null);
    }

    public void run(String arg, Closure stdoutLineClosure) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        run(extractArgs(arg), stdoutLineClosure, null);
    }

    public void run(String arg, Closure stdoutLineClosure, Closure stderrLineClosure) {
        Assert.isNotBlank(arg, "Arguments must not be null!");
        run(extractArgs(arg), stdoutLineClosure, stderrLineClosure);
    }

    public void run(List<String> args, Closure stdoutLineClosure, Closure stderrLineClosure) {
        commandLineArgs = args;
        this.stdoutLineConsumer =
                stdoutLineClosure == null ? LOGGER::quiet : new ClosureLineConsumer(stdoutLineClosure);
        this.stderrLineConsumer =
                stderrLineClosure == null ? LOGGER::error : new ClosureLineConsumer(stderrLineClosure);
    }

    public void run(List<String> args, Consumer<String> stdoutLineConsumer, Consumer<String> stderrLineConsumer) {
        commandLineArgs = args;
        this.stdoutLineConsumer = stdoutLineConsumer;
        this.stderrLineConsumer = stderrLineConsumer;
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

    private List<String> extractArgs(String arg) {
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
                this.file = new File(getProject().getProjectDir(), file);
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
