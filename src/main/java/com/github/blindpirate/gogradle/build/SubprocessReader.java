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

import com.github.blindpirate.gogradle.util.ExceptionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.blindpirate.gogradle.GogradleGlobal.DEFAULT_CHARSET;

public class SubprocessReader extends Thread {
    private Supplier<InputStream> is;
    private CountDownLatch latch;
    private Consumer<String> consumer;

    public SubprocessReader(Supplier<InputStream> is,
                            Consumer<String> consumer,
                            CountDownLatch latch) {
        this.is = is;
        this.latch = latch;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is.get(), DEFAULT_CHARSET))) {
            String line;
            while ((line = br.readLine()) != null) {
                consumer.accept(line);
            }
        } catch (IOException e) {
            consumer.accept(ExceptionHandler.getStackTrace(e));
        } finally {
            latch.countDown();
        }
    }
}
