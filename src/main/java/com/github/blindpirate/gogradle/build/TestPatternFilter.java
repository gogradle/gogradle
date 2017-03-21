package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.common.GoSourceCodeFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

public class TestPatternFilter extends GoSourceCodeFilter {
    public static TestPatternFilter withPattern(List<String> patterns) {
        WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(patterns);
        Predicate<File> andPredicate = (file) -> wildcardFileFilter.accept(file) && isTestGoFile(file);
        return new TestPatternFilter(andPredicate);
    }

    private TestPatternFilter(Predicate<File> filePredicate) {
        super(filePredicate);
    }
}
