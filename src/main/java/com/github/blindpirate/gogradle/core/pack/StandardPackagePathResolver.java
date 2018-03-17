/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

// https://golang.org/pkg/#stdlib
@Singleton
public class StandardPackagePathResolver implements PackagePathResolver {

    @Override
    @DebugLog
    public Optional<GolangPackage> produce(String packagePath) {
        Path path = Paths.get(packagePath);
        if (isStandardPackage(path)) {
            return Optional.of(StandardGolangPackage.of(path));
        } else {
            return Optional.empty();
        }
    }

    public boolean isStandardPackage(Path path) {
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
                    "plugin"
            );
}
