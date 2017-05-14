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

package com.github.blindpirate.gogradle.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.GZIPInputStream;

public class CompressUtils {
    private static final Logger LOGGER = Logging.getLogger(CompressUtils.class);

    public static void decompressZip(File zipFile, File destDir) {
        try {
            ZipFile zF = new ZipFile(zipFile);
            zF.extractAll(destDir.toString());
        } catch (ZipException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    public static void decompressTarGz(File tarGzFile, File destDir) {
        try {
            IOUtils.forceMkdir(destDir);

            TarInputStream tin = new TarInputStream(new GZIPInputStream(new FileInputStream(tarGzFile)));
            TarEntry tarEntry = tin.getNextEntry();
            while (tarEntry != null) {
                File destPath = new File(destDir, tarEntry.getName());
                if (tarEntry.isDirectory()) {
                    IOUtils.forceMkdir(destPath);
                } else {
                    FileOutputStream fout = new FileOutputStream(destPath);
                    tin.copyEntryContents(fout);
                    fout.close();
                }
                tarEntry = tin.getNextEntry();
            }
            tin.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void decompressZipOrTarGz(File compressedFile, File destDir) {
        LOGGER.quiet("Extracting {} to {}", compressedFile.getAbsolutePath(), destDir.getAbsolutePath());
        if (compressedFile.getName().endsWith("tar.gz")) {
            decompressTarGz(compressedFile, destDir);
        } else if (compressedFile.getName().endsWith("zip")) {
            decompressZip(compressedFile, destDir);
        } else {
            throw new IllegalArgumentException("Only zip and tar.gz are supported!");
        }
    }
}
