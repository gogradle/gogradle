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

package com.github.blindpirate.gogradle.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class MarkDirectoryVisitor extends SimpleFileVisitor<Path> {
    private Set<File> markedDirectories = new HashSet<>();
    private Set<File> ancestorsOfMarkedDirectories = new HashSet<>();
    private Predicate<File> dirPredicate;
    private File rootDir;

    public MarkDirectoryVisitor(File rootDir, Predicate<File> dirPredicate) {
        this.dirPredicate = dirPredicate;
        this.rootDir = rootDir;
    }

    Set<File> getMarkedDirectories() {
        return markedDirectories;
    }

    Set<File> getAncestorsOfMarkedDirectories() {
        return ancestorsOfMarkedDirectories;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs)
            throws IOException {
        File dir = dirPath.toFile();
        if (dirPredicate.test(dir)) {
            markedDirectories.add(dir);
            while (!rootDir.equals(dir)) {
                dir = dir.getParentFile();
                ancestorsOfMarkedDirectories.add(dir);
            }
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            return FileVisitResult.CONTINUE;
        }
    }
}
