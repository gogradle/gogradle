package com.github.blindpirate.gogradle.vcs;

import javax.inject.Singleton;
import java.nio.file.Path;

@Singleton
public class JazzFetcher implements PackageFetcher {
    @Override
    public void fetch(String packageName, Path location) {
    }
}
