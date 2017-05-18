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

package com.github.blindpirate.gogradle.core.cache;

import com.github.blindpirate.gogradle.util.IOUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

public class DirectorySnapshot implements Serializable {
    private String hash;

    public static DirectorySnapshot of(File projectRoot, File targetDir) {
        return new DirectorySnapshot(md5(projectRoot, targetDir));
    }

    private static String md5(File projectRoot, File targetDir) {
        List<File> allFilesInDir = IOUtils.listAllDescendents(targetDir)
                .stream().sorted(File::compareTo).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        allFilesInDir.forEach(file -> {
            Path relativePath = projectRoot.toPath().relativize(file.toPath());
            sb.append(file.isFile() ? "f" : "d").append(File.pathSeparatorChar)
                    .append(toUnixString(relativePath)).append(File.pathSeparatorChar)
                    .append(file.length()).append(File.pathSeparatorChar)
                    .append(file.lastModified()).append(File.pathSeparatorChar);
        });
        return DigestUtils.md5Hex(sb.toString());
    }

    public boolean isUpToDate(File projectRoot, File targetDir) {
        if (!targetDir.exists() || targetDir.isFile()) {
            return false;
        }
        return md5(projectRoot, targetDir).equals(hash);
    }

    private DirectorySnapshot(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }
}
