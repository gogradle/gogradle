package com.github.blindpirate.gogradle.core.pack;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Some packages are used frequently, but contain "unrecognized" packages.
 * For example, github.com/golang/mock package contains a package import declaration "a" in its test files,
 * which confuses many users. Now some packages are excluded by default
 * via {@link com.github.blindpirate.gogradle.GolangRepositoryHandler#addEmptyRepo(String)}
 *
 * @see {@link com.github.blindpirate.gogradle.GolangPluginSetting#ignoredPackages}
 */
public class GloballyIgnoredPackages {
    public static final List<String> PKGS = ImmutableList.of(
            "a", // by golang/mock
            "appengine_internal", // by GAE
            "appengine", // by GAE
            "common", // by apache/thrift
            "gen",
            "shared",
            "thrift",
            "thrifttest",
            "tutorial"
    );
}
