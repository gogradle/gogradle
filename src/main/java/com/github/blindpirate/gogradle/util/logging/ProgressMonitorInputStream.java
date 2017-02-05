package com.github.blindpirate.gogradle.util.logging;

import com.github.blindpirate.gogradle.GogradleGlobal;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.internal.service.ServiceRegistry;

import java.io.IOException;
import java.io.InputStream;

import static com.github.blindpirate.gogradle.util.IOUtils.byteCountToDisplaySize;


public class ProgressMonitorInputStream extends InputStream {
    private static final int BUF_SIZE = 4096;
    private InputStream delegate;
    private ProgressLogger logger;
    private int totalBytes;
    private int bufferedBytes;
    private boolean completed;

    public ProgressMonitorInputStream(String url, InputStream delegate) {
        this.delegate = delegate;
        ProgressLoggerFactory progressLoggerFactory =
                GogradleGlobal.getInstance(ServiceRegistry.class).get(ProgressLoggerFactory.class);
        logger = progressLoggerFactory.newOperation(this.getClass());

        String description = "Start downloading from " + url;
        logger.start(description, description);
    }

    @Override
    public int read() throws IOException {
        int ret = delegate.read();
        if (completed) {
            return ret;
        }
        if (ret == -1) {
            progress();
            logger.completed();
            completed = true;
        } else {
            bufferedBytes += 1;
            if (bufferedBytes >= BUF_SIZE) {
                progress();
            }
        }
        return ret;
    }

    private void progress() {
        totalBytes += bufferedBytes;
        bufferedBytes = 0;
        logger.progress(byteCountToDisplaySize(totalBytes) + " downloaded");
    }
}
