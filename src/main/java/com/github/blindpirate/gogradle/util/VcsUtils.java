package com.github.blindpirate.gogradle.util;

import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.base.Optional;

import java.nio.file.Path;
import java.nio.file.Paths;

public class VcsUtils {
    public static Optional<VcsType> getVcsType(String packageName) {
        Path path = Paths.get(packageName);
        for (int i = 0; i < path.getNameCount(); ++i) {
            Optional<VcsType> vcsType = VcsType.ofDotSuffix(path.getName(i).toString());
            if (vcsType.isPresent()) {
                return vcsType;
            }
        }
        return Optional.absent();
    }

    public static int vcsSuffixIndexOf(String packageName) {
        Path path = Paths.get(packageName);
        for (int i = 0; i < path.getNameCount(); ++i) {
            Optional<VcsType> vcsType = VcsType.ofDotSuffix(path.getName(i).toString());
            if (vcsType.isPresent()) {
                return i;
            }
        }
        return -1;
    }
}
