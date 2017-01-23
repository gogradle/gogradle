package com.github.blindpirate.gogradle.build;

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
        return accept(null, file.getName());
    }

    @Override
    public boolean accept(File dir, String name) {
        if (StringUtils.startsWithAny(name, "_", ".")) {
            return false;
        }
        if (!name.endsWith("_test.go")) {
            return false;
        }
        return wildcardFilter.accept(dir, name);
    }
}
