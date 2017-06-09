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

package com.github.blindpirate.gogradle.build;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Deal with environment related to a specific go build. For example, GOPATH/GOROOT to be used.
 */
public interface BuildManager {
    /**
     * Determine GOPATH to be used. If global GOPATH doesn't exist, a project-level GOPATH will be prepared.
     */
    void prepareProjectGopathIfNecessary();

    /**
     * Get GOPATH in this build.
     *
     * @return the GOPATH to be used
     */
    String getGopath();

    /**
     * Fork a go process and run commands specified by {@code args}, under the environments
     * comprised by {@code env} + GOPATH/GOROOT/GOARCH/GOEXE, where {@code env} has higher priority.
     *
     * @param args the arguments to be passed to go
     * @param env  extra environment variables to be passed to go
     * @return return code of go command
     */
    int go(List<String> args, Map<String, String> env);

    /**
     * Fork a go process and run commands specified by {@code args}, under the environments
     * comprised by {@code env} + GOPATH/GOROOT/GOARCH/GOEXE, where {@code env} has higher priority.
     * <p>
     * Stdout and stderr line of the forked process will be consumed by {@code stdoutLineConsumer} and
     * {@code stderrLineConsumer} line by line, respectively.
     * <p>
     * Return code of the forked process will be consumed by {@code retcodeConsumer}.
     *
     * @param args               the arguments to be passed to go
     * @param env                extra environment variables to be passed to go
     * @param stdoutLineConsumer the consumer by which stdout line is consumed
     * @param stderrLineConsumer the consumer by which stderr line is consumed
     * @param retcodeConsumer    the consumer by which ret code is consumed
     * @return return code of go command
     */
    int go(List<String> args,
           Map<String, String> env,
           Consumer<String> stdoutLineConsumer,
           Consumer<String> stderrLineConsumer,
           Consumer<Integer> retcodeConsumer);

    /**
     * Fork a process and run commands specified by {@code args}, under the environments
     * comprised by {@code env} + GOPATH/GOROOT/GOARCH/GOEXE, where {@code env} has higher priority.
     * <p>
     * Stdout and stderr line of the forked process will be consumed by {@code stdoutLineConsumer} and
     * {@code stderrLineConsumer} line by line, respectively.
     * <p>
     * Return code of the forked process will be consumed by {@code retcodeConsumer}.
     *
     * @param args               the arguments start a process
     * @param env                extra environment variables to be passed to go
     * @param stdoutLineConsumer the consumer by which stdout line is consumed
     * @param stderrLineConsumer the consumer by which stderr line is consumed
     * @param retcodeConsumer    the consumer by which ret code is consumed
     * @return return code of go command
     */
    int run(List<String> args,
            Map<String, String> env,
            Consumer<String> stdoutLineConsumer,
            Consumer<String> stderrLineConsumer,
            Consumer<Integer> retcodeConsumer);

}
