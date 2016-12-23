package com.github.blindpirate.gogradle.core.pack;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Singleton
public class StandardPackageNameResolver implements PackageNameResolver {

    @Override
    public Optional<PackageInfo> produce(String packageName) {
        Path path = Paths.get(packageName);
        if (isStandardPackage(path)) {
            return Optional.of(PackageInfo.standardPackage(packageName));
        } else {
            return Optional.absent();
        }
    }

    private boolean isStandardPackage(Path path) {
        return path.getNameCount() > 0
                && standardFirstLevelPackages.contains(path.getName(0).toString());
    }

    // https://golang.org/pkg/
    private final Set<String> standardFirstLevelPackages =
            Sets.newHashSet("archive",
                    "bufio",
                    "builtin",
                    "bytes",
                    "compress",
                    "container",
                    "context",
                    "crypto",
                    "database",
                    "debug",
                    "encoding",
                    "errors",
                    "expvar",
                    "flag",
                    "fmt",
                    "go",
                    "hash",
                    "html",
                    "image",
                    "index",
                    "io",
                    "log",
                    "math",
                    "mime",
                    "net",
                    "os",
                    "path",
                    "reflect",
                    "regexp",
                    "runtime",
                    "sort",
                    "strconv",
                    "strings",
                    "sync",
                    "syscall",
                    "testing",
                    "text",
                    "time",
                    "unicode",
                    "unsafe");
}
