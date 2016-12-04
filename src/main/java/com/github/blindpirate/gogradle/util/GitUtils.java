package com.github.blindpirate.gogradle.util;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;

import java.util.Set;

public class GitUtils {

    public static Set<String> getRemoteUrl(Repository repository) {
        Config config = repository.getConfig();
        return config.getSubsections("remote");
    }
}
