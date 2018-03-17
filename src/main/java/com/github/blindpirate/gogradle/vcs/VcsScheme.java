package com.github.blindpirate.gogradle.vcs;

// https://github.com/golang/go/blob/1102616c772c262175f5ba5f12d6b574d0ad9101/src/cmd/go/internal/get/vcs.go
public enum VcsScheme {
    GIT("git://"),
    HTTPS("https://"),
    HTTP("http://"),
    GIT_SSH("git+ssh://"),
    SSH("ssh://"),
    BZR("bzr://"),
    BZR_SSH("bzr+ssh://"),
    SVN("svn://"),
    SVN_SSH("svn+ssh://");

    private String scheme;

    VcsScheme(String scheme) {
        this.scheme = scheme;
    }

    public String buildUrl(String packagePath) {
        return scheme + packagePath;
    }
}
