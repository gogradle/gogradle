package com.github.blindpirate.gogradle.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
            throw ExceptionHandler.uncheckException(e);
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
