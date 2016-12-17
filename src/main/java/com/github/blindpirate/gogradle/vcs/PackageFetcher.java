package com.github.blindpirate.gogradle.vcs;

import java.nio.file.Path;

/**
 * Fetches a package specified by name.
 */
public interface PackageFetcher {
    String HTTPS = "https://";
    String HTTP = "http://";
    String SSH = "ssh://";

    void fetch(String packageName, Path location);
}
