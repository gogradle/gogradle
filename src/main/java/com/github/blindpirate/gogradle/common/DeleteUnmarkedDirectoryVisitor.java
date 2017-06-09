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

import com.github.blindpirate.gogradle.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class DeleteUnmarkedDirectoryVisitor extends SimpleFileVisitor<Path> {
    private MarkDirectoryVisitor markDirectoryVisitor;

    public DeleteUnmarkedDirectoryVisitor(MarkDirectoryVisitor markDirectoryVisitor) {
        this.markDirectoryVisitor = markDirectoryVisitor;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs)
            throws IOException {
        File dir = dirPath.toFile();
        if (isMarked(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        } else if (isAncestorOfMarked(dir)) {
            return FileVisitResult.CONTINUE;
        } else {
            IOUtils.forceDelete(dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
    }

    private boolean isMarked(File dir) {
        return markDirectoryVisitor.getMarkedDirectories().contains(dir);
    }

    private boolean isAncestorOfMarked(File dir) {
        return markDirectoryVisitor.getAncestorsOfMarkedDirectories().contains(dir);
    }
}
