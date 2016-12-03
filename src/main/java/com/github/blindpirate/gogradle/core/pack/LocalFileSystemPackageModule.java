package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.FileSystemPackageModule;
import com.github.blindpirate.gogradle.core.GolangPackageModule;

import java.io.File;
import java.nio.file.Path;

public class LocalFileSystemPackageModule extends FileSystemPackageModule {

    public LocalFileSystemPackageModule(String name, Path rootDir) {
        super(name, rootDir);
    }

    @Override
    public FileSystemPackageModule vendor(Path relativePathToVendor) {
        Path relativeToParent = addVendorPrefix(relativePathToVendor);
        LocalFileSystemPackageModule ret = new LocalFileSystemPackageModule(
                relativePathToVendor.toString(),
                this.getRootDir().resolve(relativeToParent));
        return ret;
    }

    public static GolangPackageModule fromFileSystem(String name, File rootDir) {
        LocalFileSystemPackageModule ret = new LocalFileSystemPackageModule(name, rootDir.toPath());
        ret.setUpdateTime(rootDir.lastModified());
        return ret;
    }
}
