package com.github.blindpirate.gogradle.core.dependency.install;

import com.github.blindpirate.gogradle.util.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import static com.github.blindpirate.gogradle.core.dependency.produce.SourceCodeDependencyFactory.TESTDATA_DIRECTORY;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.StringUtils.endsWithAny;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameEqualsAny;
import static com.github.blindpirate.gogradle.util.StringUtils.fileNameStartsWithAny;
import static com.github.blindpirate.gogradle.util.StringUtils.startsWithAny;

public enum DependencyInstallFileFilter implements FileFilter {

    INSTANCE;

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return acceptDirectory(pathname);
        } else {
            return acceptFile(pathname);
        }
    }

    private boolean acceptFile(File file) {
        return acceptFileName(file.getName());
    }

    private boolean acceptFileName(String name) {
        if (startsWithAny(name, "_", ".")) {
            return false;
        }
        if (endsWithAny(name, "_test.go")) {
            return false;
        }

        return endsWithAny(name, ".go", ".asm", ".s");
    }

    private boolean acceptDirectory(File dir) {
        if (fileNameEqualsAny(dir, TESTDATA_DIRECTORY, VENDOR_DIRECTORY)) {
            return false;
        }
        if (fileNameStartsWithAny(dir, "_", ".")) {
            return false;
        }

        List<String> files = IOUtils.safeList(dir);
        return files.stream()
                .map(name -> dir.toPath().resolve(name).toFile())
                .anyMatch(this::accept);
    }
}
