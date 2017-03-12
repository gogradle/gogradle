package com.github.blindpirate.gogradle.common;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;

@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public abstract class AbstractFileFilter extends org.apache.commons.io.filefilter.AbstractFileFilter {
    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return acceptDir(file);
        } else {
            return acceptFile(file);
        }
    }

    protected abstract boolean acceptFile(File file);

    protected abstract boolean acceptDir(File dir);
}
