package com.github.blindpirate.gogradle.common;

import com.github.blindpirate.gogradle.util.StringUtils;

import java.io.File;
import java.util.function.Predicate;

import static com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory.TESTDATA_DIRECTORY;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameEqualsAny;

public class GoSourceCodeFilter extends AbstractFileFilter {

    private Predicate<File> filePredicate;

    public static final GoSourceCodeFilter BUILD_GO_FILTER = withFilePredicate(GoSourceCodeFilter::isBuildGoFile);
    public static final GoSourceCodeFilter TEST_GO_FILTER = withFilePredicate(GoSourceCodeFilter::isTestGoFile);

    protected static boolean isBuildGoFile(File file) {
        return file.getName().endsWith(".go") && !file.getName().endsWith("_test.go");
    }

    protected static boolean isTestGoFile(File file) {
        return file.getName().endsWith("_test.go");
    }

    public static GoSourceCodeFilter withFilePredicate(Predicate<File> predicate) {
        return new GoSourceCodeFilter(predicate);
    }

    protected GoSourceCodeFilter(Predicate<File> filePredicate) {
        this.filePredicate = filePredicate;
    }

    @Override
    protected boolean acceptFile(File file) {
        if (StringUtils.fileNameStartsWithAny(file, "_", ".")) {
            return false;
        }
        return filePredicate.test(file);
    }

    @Override
    protected boolean acceptDir(File dir) {
        if (StringUtils.fileNameStartsWithAny(dir, "_", ".")) {
            return false;
        }
        if (fileNameEqualsAny(dir, TESTDATA_DIRECTORY, VENDOR_DIRECTORY)) {
            return false;
        }
        return true;
    }
}
