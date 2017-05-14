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

package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.GogradleGlobal;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.internal.service.ServiceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GitClientLineConsumer implements Consumer<String> {

    public static final GitClientLineConsumer NO_OP = new GitClientLineConsumer() {
        @Override
        public void accept(String line) {
        }

        @Override
        public void complete() {
        }
    };

    private ProgressLogger logger;

    private List<String> lines = new ArrayList<>();

    public static GitClientLineConsumer of(String desc) {
        return new GitClientLineConsumer(desc);
    }

    private GitClientLineConsumer() {
    }

    protected GitClientLineConsumer(String desc) {
        ProgressLoggerFactory progressLoggerFactory =
                GogradleGlobal.getInstance(ServiceRegistry.class).get(ProgressLoggerFactory.class);
        logger = progressLoggerFactory.newOperation(this.getClass());
        logger.start(desc, desc);
    }

    @Override
    public void accept(String s) {
        logger.progress(s);
        lines.add(s);
    }

    public String getOutput() {
        return String.join("\n", lines);
    }

    public void complete() {
        logger.completed();
    }
}
