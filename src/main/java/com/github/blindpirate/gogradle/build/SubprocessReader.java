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
            try {
                while ((line = br.readLine()) != null) {
                    consumer.accept(line);
                }
            } catch (IOException e) {
                consumer.accept(ExceptionHandler.getStackTrace(e));
            }
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        } finally {
            latch.countDown();
        }
    }
}
