package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.FileSystemPackageModule;
import com.github.blindpirate.gogradle.core.GolangPackageModule;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;

public class LocalFileSystemPackageModule extends FileSystemPackageModule {

    public LocalFileSystemPackageModule(String name, Path rootDir) {
        super(name, rootDir);
    }

    @Override
    public FileSystemPackageModule vendor(Path relativePathToVendor) {
        LocalFileSystemPackageModule ret = new LocalFileSystemPackageModule(
                relativePathToVendor.toString(),
                relativePathToVendor.resolve(this.getRootDir()));
        return ret;
    }

    public static GolangPackageModule fromFileSystem(String name, File rootDir) {
        LocalFileSystemPackageModule ret = new LocalFileSystemPackageModule(name, rootDir.toPath());
        ret.updateTime = new Date(rootDir.lastModified());
        return ret;
    }
}
