package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.FileSystemModule;
import com.github.blindpirate.gogradle.core.GolangPackageModule;

import java.io.File;
import java.nio.file.Path;

public class LocalFileSystemModule extends FileSystemModule {

    public LocalFileSystemModule(String name, Path rootDir) {
        super(name, rootDir);
    }

    @Override
    public FileSystemModule vendor(Path relativePathToVendor) {
        Path relativeToParent = addVendorPrefix(relativePathToVendor);
        LocalFileSystemModule ret = new LocalFileSystemModule(
                relativePathToVendor.toString(),
                this.getRootDir().resolve(relativeToParent));
        return ret;
    }

    public static GolangPackageModule fromFileSystem(String name, File rootDir) {
        LocalFileSystemModule ret = new LocalFileSystemModule(name, rootDir.toPath());
        ret.setUpdateTime(rootDir.lastModified());
        return ret;
    }
}
