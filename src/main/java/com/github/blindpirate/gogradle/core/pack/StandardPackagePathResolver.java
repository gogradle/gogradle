package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.StandardGolangPackage;
import com.github.blindpirate.gogradle.util.logging.DebugLog;
import com.google.common.collect.Sets;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

@Singleton
public class StandardPackagePathResolver implements PackagePathResolver {

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        Path path = Paths.get(packagePath);
        if (isStandardPackage(path)) {
            return Optional.of(StandardGolangPackage.of(packagePath));
        } else {
            return Optional.empty();
        }
    }

    private boolean isStandardPackage(Path path) {
        return path.getNameCount() > 0
                && standardFirstLevelPackages.contains(path.getName(0).toString());
    }

    // https://golang.org/pkg/
    private final Set<String> standardFirstLevelPackages =
            Sets.newHashSet(
                    "C", // a pseudo-package, see https://golang.org/cmd/cgo/
                    "archive",
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
                    "unsafe",
                    // since Go 1.8
                    "plugin",
                    "appengine",
                    "appengine_internal"
                    );
}
