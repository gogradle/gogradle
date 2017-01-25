package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory;
import com.github.blindpirate.gogradle.util.StringUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.List;

public class TestPatternFilter implements IOFileFilter {
    private final WildcardFileFilter wildcardFilter;

    public TestPatternFilter(List<String> namePatterns) {
        this.wildcardFilter = new WildcardFileFilter(namePatterns);
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return acceptDir(file);
        } else {
            return acceptFile(file);
        }
    }

    private boolean acceptFile(File file) {
        if (StringUtils.fileNameStartsWithAny(file, "_", ".")) {
            return false;
        }
        if (!file.getName().endsWith("_test.go")) {
            return false;
        }
        return wildcardFilter.accept(file);
    }

    private boolean acceptDir(File dir) {
        if (StringUtils.fileNameStartsWithAny(dir, "_", ".")) {
            return false;
        }
        if (SourceCodeDependencyFactory.TESTDATA_DIRECTORY.equals(dir.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean accept(File dir, String name) {
        return accept(new File(dir, name));
    }
}
