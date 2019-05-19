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
import com.github.blindpirate.gogradle.util.ConfigureUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.GolangPlugin.GOGRADLE_INJECTOR;
import static com.github.blindpirate.gogradle.task.GolangTaskContainer.PREPARE_TASK_NAME;

/**
 * This class is designed to run custom go and other commands with build-scope environment variables,
 * such as GOPATH, GOROOT, etc. A typical usage is to declare a task:
 * <pre>
 * {@code
 * task myTask(type: Go) {
 *     dependsOn 'goVendor'
 *     go 'build -o /my/output/location --my-own-cmd-arguments github.com/my/package/cmd' {
 *        environment 'key1', 'value1'
 *        stdout appendTo('./my.log')
 *        stderr devNull()
 *     }
 * }
 * }
 * </pre>
 */
public class Go extends AbstractGolangTask {
    protected static final Logger LOGGER = Logging.getLogger(Go.class);

    protected BuildManager buildManager;
    protected Consumer<String> stdoutLineConsumer;
    protected Consumer<String> stderrLineConsumer;
    protected Map<String, String> environment = new HashMap<>();
    protected Boolean continueOnFailure;
    protected List<Integer> exitValues = new ArrayList<>();
    protected List<GoAction> goActions = new ArrayList<>();

    public Go() {
        setDescription("Custom go task.");
        dependsOn(PREPARE_TASK_NAME);
        Injector injector = (Injector) getProject().getExtensions().getByName(GOGRADLE_INJECTOR);
        this.buildManager = injector.getInstance(BuildManager.class);
    }

    @TaskAction
    public void executeTask() {
        setGogradleGlobalContext();
        exitValues = goActions.stream().map(Supplier::get).collect(Collectors.toList());
    }

    public void environment(Map<String, String> map) {
        environment.putAll(map);
    }

    public void environment(String key, String value) {
        environment.put(key, value);
    }

    @Deprecated
    public void setContinueWhenFail(Boolean continueWhenFail) {
        LOGGER.warn("continueWhenFail is deprecated, please use continueOnFailure instead.");
        this.continueOnFailure = continueWhenFail;
    }

    public void setContinueOnFailure(Boolean continueWhenFail) {
        this.continueOnFailure = continueWhenFail;
    }

    public List<GoAction> getGoActions() {
        return goActions;
    }

    public void go(String arg) {
        go(arg, null);
    }

    public void go(String arg, Closure configureClosure) {
        Assert.isNotBlank(arg, "Arguments must not be blank!");
        go(extractArgs(arg), configureClosure);
    }

    public void go(List<String> args) {
        go(args, null);
    }

    public void go(List<String> args,
                   Closure configureClosure) {
        goOrRun(args, configureClosure, buildManager::go);
    }

    public void run(String arg) {
        run(extractArgs(arg), null);
    }

    public void run(String arg, Closure configureClosure) {
        Assert.isNotBlank(arg, "Arguments must not be blank!");
        run(extractArgs(arg), configureClosure);
    }

    public void run(List<String> args) {
        run(args, null);
    }

    public void run(List<String> args, Closure configureClosure) {
        goOrRun(args, configureClosure, buildManager::run);
    }

    public Consumer<String> appendTo(String file) {
        return new FileWritingConsumer(file, true);
    }

    public Consumer<String> writeTo(String file) {
        return new FileWritingConsumer(file, false);
    }

    public Consumer<String> devNull() {
        return line -> {
        };
    }

    public void stdout(Consumer<String> consumer) {
        this.stdoutLineConsumer = consumer;
    }

    public void stderr(Consumer<String> consumer) {
        this.stderrLineConsumer = consumer;
    }

    public void stdout(Closure closure) {
        this.stdoutLineConsumer = new ClosureLineConsumer(closure);
    }

    public void stderr(Closure closure) {
        this.stderrLineConsumer = new ClosureLineConsumer(closure);
    }

    public List<Integer> getExitValues() {
        return exitValues;
    }

    public Integer getExitValue() {
        if (exitValues.size() == 1) {
            return exitValues.get(0);
        } else {
            return exitValues.stream().allMatch(value -> value == 0) ? 0 : -1;
        }
    }

    public void addGoAction(GoAction goAction) {
        this.goActions.add(new GoAction(goAction));
    }

    private void goOrRun(List<String> args,
                         Closure configureClosure,
                         BuildFunction function) {
        Assert.isNotEmpty(args, "Arguments must not be blank!");
        GoAction action = new GoAction(args, function);
        ConfigureUtils.configure(action, configureClosure);

        goActions.add(action);
    }

    private List<String> extractArgs(String arg) {
        return Arrays.asList(Commandline.translateCommandline(arg));
    }

    public class GoAction implements Supplier<Integer> {
        private final List<String> args;
        private final BuildFunction function;
        private Consumer<String> stdoutLineConsumer;
        private Consumer<String> stderrLineConsumer;
        private Map<String, String> environment = new HashMap<>();
        private Boolean continueOnFailure;

        protected GoAction() {
            args = null;
            function = null;
        }

        private GoAction(List<String> args, BuildFunction function) {
            this.args = args;
            this.function = function;
        }

        public GoAction(GoAction instance) {
            this.args = instance.args;
            this.function = instance.function;
            this.stdoutLineConsumer = instance.stdoutLineConsumer;
            this.stderrLineConsumer = instance.stderrLineConsumer;
            this.environment = new HashMap<>(instance.environment);
            this.continueOnFailure = instance.continueOnFailure;
        }

        @Override
        public Integer get() {
            return function.apply(args,
                    getEnvironment(),
                    getStdoutLineConsumer(),
                    getStderrLineConsumer(),
                    isContinueOnFailure());
        }

        public void environment(Map<String, String> map) {
            environment.putAll(map);
        }

        public void environment(String key, String value) {
            environment.put(key, value);
        }

        public void stdout(Consumer<String> consumer) {
            this.stdoutLineConsumer = consumer;
        }

        public void stderr(Consumer<String> consumer) {
            this.stderrLineConsumer = consumer;
        }

        public void stdout(Closure closure) {
            this.stdoutLineConsumer = new ClosureLineConsumer(closure);
        }

        public void stderr(Closure closure) {
            this.stderrLineConsumer = new ClosureLineConsumer(closure);
        }

        public Consumer<String> appendTo(String file) {
            return Go.this.appendTo(file);
        }

        public Consumer<String> writeTo(String file) {
            return Go.this.writeTo(file);
        }

        public Consumer<String> devNull() {
            return Go.this.devNull();
        }

        public void setContinueOnFailure(Boolean continueOnFailure) {
            this.continueOnFailure = continueOnFailure;
        }

        public Consumer<String> getStdoutLineConsumer() {
            if (stdoutLineConsumer != null) {
                return stdoutLineConsumer;
            }
            if (Go.this.stdoutLineConsumer != null) {
                return Go.this.stdoutLineConsumer;
            }
            return LOGGER::quiet;
        }

        public Consumer<String> getStderrLineConsumer() {
            if (stderrLineConsumer != null) {
                return stderrLineConsumer;
            }
            if (Go.this.stderrLineConsumer != null) {
                return Go.this.stderrLineConsumer;
            }
            return LOGGER::error;
        }

        public Map<String, String> getEnvironment() {
            Map<String, String> outsideEnvs = new HashMap<>(Go.this.environment);
            outsideEnvs.putAll(environment);
            return outsideEnvs;
        }

        public boolean isContinueOnFailure() {
            if (continueOnFailure != null) {
                return continueOnFailure;
            }
            if (Go.this.continueOnFailure != null) {
                return Go.this.continueOnFailure;
            }
            return false;
        }

    }

    private class FileWritingConsumer implements Consumer<String> {
        private File file;

        private FileWritingConsumer(String file, boolean append) {
            Path filePath = Paths.get(file);
            if (filePath.isAbsolute()) {
                this.file = filePath.toFile();
            } else {
                this.file = new File(getProjectDir(), file);
            }

            if (!append) {
                IOUtils.write(this.file, "");
            }
        }

        @Override
        public void accept(String line) {
            IOUtils.append(file, line + "\n");
        }
    }

    public static class ClosureLineConsumer implements Consumer<String> {
        private Closure closure;

        public ClosureLineConsumer(Closure closure) {
            this.closure = closure;
        }

        @Override
        public void accept(String s) {
            closure.call(s);
        }
    }

    public interface BuildFunction {
        int apply(List<String> args,
                  Map<String, String> env,
                  Consumer<String> stdoutLineConsumer,
                  Consumer<String> stderrLineConsumer,
                  boolean continueOnFailure);
    }

}
