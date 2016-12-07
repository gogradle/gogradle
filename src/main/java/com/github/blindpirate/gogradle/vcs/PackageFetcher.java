package com.github.blindpirate.gogradle.vcs;

import java.nio.file.Path;

public interface PackageFetcher {
    String HTTPS = "https://";
    String HTTP = "http://";
    String SSH = "ssh://";

    void fetch(String packageName, Path location);
}
