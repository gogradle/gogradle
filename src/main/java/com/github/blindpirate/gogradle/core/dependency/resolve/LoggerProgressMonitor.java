package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.GogradleGlobal;
import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.internal.service.ServiceRegistry;

public class LoggerProgressMonitor extends BatchingProgressMonitor {

    private ProgressLogger logger;

    private static final int MAGIC_25 = 25;
    private static final int MAGIC_10 = 10;
    private static final int MAGIC_100 = 100;
    private static final int PADDING_SPACE_COUNT = 80;


    public LoggerProgressMonitor(String action, String url) {
        ProgressLoggerFactory progressLoggerFactory =
                GogradleGlobal.getInstance(ServiceRegistry.class).get(ProgressLoggerFactory.class);
        logger = progressLoggerFactory.newOperation(this.getClass());
        String desc = action + " from " + url;
        logger.start(desc, desc);
    }

    public void completed() {
        logger.completed();
    }

    @Override
    protected void onUpdate(String taskName, int workCurr) {
        StringBuilder s = new StringBuilder();
        format(s, taskName, workCurr);
        logger.progress(s.toString());
    }

    @Override
    protected void onEndTask(String taskName, int workCurr) {
        StringBuilder s = new StringBuilder();
        format(s, taskName, workCurr);
        s.append("\n"); //$NON-NLS-1$
        logger.progress(s.toString());
    }

    private void format(StringBuilder s, String taskName, int workCurr) {
        s.append("\r"); //$NON-NLS-1$
        s.append(taskName);
        s.append(": "); //$NON-NLS-1$
        while (s.length() < MAGIC_25) {
            s.append(' ');
        }
        s.append(workCurr);
        for (int i = 0; i < PADDING_SPACE_COUNT; ++i) {
            s.append(" ");
        }
    }

    @Override
    protected void onUpdate(String taskName, int cmp, int totalWork, int pcnt) {
        StringBuilder s = new StringBuilder();
        format(s, taskName, cmp, totalWork, pcnt);
        logger.progress(s.toString());
    }

    @Override
    protected void onEndTask(String taskName, int cmp, int totalWork, int pcnt) {
        StringBuilder s = new StringBuilder();
        format(s, taskName, cmp, totalWork, pcnt);
        s.append("\n"); //$NON-NLS-1$
        logger.progress(s.toString());
    }

    private void format(StringBuilder s, String taskName, int cmp,
                        int totalWork, int pcnt) {
        s.append("\r"); //$NON-NLS-1$
        s.append(taskName);
        s.append(": "); //$NON-NLS-1$
        while (s.length() < MAGIC_25) {
            s.append(' ');
        }

        String endStr = String.valueOf(totalWork);
        String curStr = String.valueOf(cmp);
        while (curStr.length() < endStr.length()) {
            curStr = " " + curStr; //$NON-NLS-1$
        }
        if (pcnt < MAGIC_100) {
            s.append(' ');
        }
        if (pcnt < MAGIC_10) {
            s.append(' ');
        }
        s.append(pcnt);
        s.append("% ("); //$NON-NLS-1$
        s.append(curStr);
        s.append("/"); //$NON-NLS-1$
        s.append(endStr);
        s.append(")"); //$NON-NLS-1$

        for (int i = 0; i < PADDING_SPACE_COUNT; ++i) {
            s.append(" ");
        }
    }
}
