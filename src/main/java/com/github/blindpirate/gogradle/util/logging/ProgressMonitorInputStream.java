package com.github.blindpirate.gogradle.util.logging;

import com.github.blindpirate.gogradle.GogradleGlobal;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.internal.service.ServiceRegistry;

import java.io.IOException;
import java.io.InputStream;

import static com.github.blindpirate.gogradle.util.IOUtils.byteCountToDisplaySize;


public class ProgressMonitorInputStream extends InputStream {
    private InputStream delegate;
    private ProgressLogger logger;
    private int totalBytes;
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
        int bytes = delegate.read();
        if (completed) {
            return bytes;
        }
        if (bytes == -1) {
            logger.completed();
            completed = true;
        } else {
            totalBytes += bytes;
            logger.progress(byteCountToDisplaySize(totalBytes) + " downloaded");
        }
        return bytes;
    }
}
