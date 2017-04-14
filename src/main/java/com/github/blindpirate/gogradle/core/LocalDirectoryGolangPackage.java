package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

public class LocalDirectoryGolangPackage extends ResolvableGolangPackage {

    private String dir;

    private LocalDirectoryGolangPackage(Path rootPath, Path path, String dir) {
        super(rootPath, path);
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

    @Override
    protected Optional<GolangPackage> longerPath(Path packagePath) {
        return Optional.of(of(getRootPath(), packagePath, dir));
    }

    @Override
    protected Optional<GolangPackage> shorterPath(Path packagePath) {
        if (StringUtils.toUnixString(packagePath).length() < getRootPathString().length()) {
            return Optional.of(IncompleteGolangPackage.of(packagePath));
        } else {
            return Optional.of(of(getRootPath(), packagePath, dir));
        }
    }

    public static LocalDirectoryGolangPackage of(Path rootPath, Path path, String dir) {
        return new LocalDirectoryGolangPackage(rootPath, path, dir);
    }

    public static LocalDirectoryGolangPackage of(String rootPath, String path, String dir) {
        return new LocalDirectoryGolangPackage(Paths.get(rootPath), Paths.get(path), dir);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        LocalDirectoryGolangPackage that = (LocalDirectoryGolangPackage) o;
        return Objects.equals(dir, that.dir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dir);
    }
}
